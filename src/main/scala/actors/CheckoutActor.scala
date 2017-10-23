package actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.language.postfixOps

object CheckoutActor  {
  case class Cancelled()
  case class DeliveryMethodSelected()
  case class CheckoutStarted()
  case class PaymentSelected()
  case class PaymentReceived()
  case class CheckoutTimerExpired()
  case class PaymentTimerExpired()

  def props(cart: ActorRef): Props = Props(new CheckoutActor(cart))
}

class CheckoutActor(cart: ActorRef) extends Actor with Timers{
  import CheckoutActor._

  def receive = LoggingReceive {
    case CheckoutStarted() =>
      timers.startSingleTimer(CheckoutTimerExpired, CheckoutTimerExpired, 3.seconds)
      context become selectingDelivery()
  }

  def selectingDelivery(): Receive = LoggingReceive {
    case Cancelled =>
      cart ! CartActor.CheckoutCancelled
      context stop self
    case DeliveryMethodSelected =>
      context become selectingPaymentMethod()
    case CheckoutTimerExpired =>
      cart ! CartActor.CheckoutCancelled
      context stop self
  }

  def processingPayment(): Receive = LoggingReceive {
    case PaymentReceived =>
      timers.cancel(PaymentTimerExpired)
      cart ! CartActor.CheckoutClosed
      context stop self
    case PaymentTimerExpired =>
      cart ! CartActor.CheckoutCancelled
      context stop self
  }

  def selectingPaymentMethod(): Receive = LoggingReceive {
    case Cancelled =>
      cart ! CartActor.CheckoutCancelled
      context stop self
    case PaymentSelected =>
      timers.cancel(CheckoutTimerExpired)
      timers.startSingleTimer(PaymentTimerExpired, PaymentTimerExpired, 3.seconds)
      context become processingPayment()
    case CheckoutTimerExpired =>
      cart ! CartActor.CheckoutCancelled
      context stop self
  }
}
