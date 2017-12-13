package actors

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props, Timers}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration._
import scala.language.postfixOps

object CheckoutActor {
  case class Cancelled()
  case class DeliveryMethodSelected()
  case class PaymentSelected(date: Long = System.currentTimeMillis())
  case class PaymentReceived()
  case class CheckoutTimerExpired()
  case class PaymentTimerExpired()

  case class CheckoutTimerStartedEvent(timestamp: Long)
  case class PaymentTimerStartedEvent(timestamp: Long)
}

class CheckoutActor(id: Long, customer: ActorRef) extends PersistentActor with Timers {
  import CartManagerActor._
  import CheckoutActor._

  private val cart: ActorRef = context.parent

  val checkoutTimer = "CheckoutTimer"
  val checkoutTimeout = FiniteDuration(10, TimeUnit.SECONDS)
  val paymentTimer = "PaymentTimer"
  val paymentTimeout = FiniteDuration(10, TimeUnit.SECONDS)
  val interval: FiniteDuration = 10 seconds

  override def persistenceId: String = "checkout:" + id

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case event: Any => updateState(event)
  }

  override def receiveCommand: Receive = LoggingReceive {
    case event: CheckoutStarted => persist(event)(event => updateState(event))
  }

  def selectingDelivery: Receive = LoggingReceive {
    case DeliveryMethodSelected =>
      persist(DeliveryMethodSelected)(event => updateState(event))
    case CheckoutCancelled =>
      persist(CheckoutCancelled)(event => updateState(event))
    case CheckoutTimerExpired =>
      persist(CheckoutTimerExpired)(event => updateState(event))
  }

  def processingPayment: Receive = LoggingReceive {
    case PaymentReceived => persist(PaymentReceived)( event => {
      customer ! CheckoutClosed
      cart ! CheckoutClosed
      updateState(event)
    })
    case PaymentTimerExpired => persist(PaymentTimerExpired)(event => {
      cart ! CheckoutCancelled
      updateState(event)
    })
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case event: PaymentSelected =>
      persist(event)(event => {
        val paymentService: ActorRef = context actorOf Props(classOf[PaymentServiceActor], customer)
        customer ! CustomerActor.PaymentServiceStarted(paymentService)
        updateState(event)
      })
    case Cancelled =>
      persist(Cancelled)(event => {
        cart ! CheckoutCancelled
        updateState(event)
      })
    case CheckoutTimerExpired =>
      persist(CheckoutTimerExpired)(event => {
        cart ! CheckoutCancelled
        customer ! CheckoutCancelled
        updateState(event)
      })
  }

  private def updateState(event: Any): Unit = {
    event match {
      case event: CheckoutStarted =>
        context become selectingDelivery
        startTimer(event.date, checkoutTimer, checkoutTimeout, CheckoutTimerExpired)
      case DeliveryMethodSelected =>
        context become selectingPaymentMethod
      case CheckoutCancelled | Cancelled =>
        timers.cancelAll()
        context stop self
      case CheckoutTimerExpired =>
        timers.cancel(checkoutTimer)
        context stop self
      case event: PaymentSelected =>
        timers.cancel(checkoutTimer)
        startTimer(event.date, paymentTimer, paymentTimeout, PaymentTimerExpired)
        context become processingPayment
      case PaymentTimerExpired =>
        timers.cancel(paymentTimer)
        context stop self
      case PaymentReceived =>
        timers.cancel(paymentTimer)
        context stop self
    }
  }

  private def startTimer(startTime: Long, timer: String, timeout: FiniteDuration, event: Any): Unit = {
    val timeLeft = (startTime + timeout.toMillis) - System.currentTimeMillis()
    if (timeLeft > 0) timers.startSingleTimer(timer, event, FiniteDuration(timeLeft, TimeUnit.MILLISECONDS))
    else updateState(event)
  }
}