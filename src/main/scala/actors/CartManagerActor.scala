package actors

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props, Timers}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object CartManagerActor  {
  case class Content(Cart: Cart)
  case class ItemRemoved(item: Item, date: Long)
  case class ItemAdded(item: Item, date: Long)
  case class CheckoutStarted(checkout: ActorRef, date: Long)
  case class CheckoutCancelled(date: Long = System.currentTimeMillis())
  case class CheckoutClosed()
  case class CartTimerExpired()

  case class Checkout(checkout: ActorRef)
}

class CartManagerActor(id: Long, var cart: Cart, checkoutId: Long) extends PersistentActor with Timers{
  import CartManagerActor._

  private val customer: ActorRef = context.parent

  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(12, TimeUnit.SECONDS)

  override def persistenceId: String = "CartManagerActor:" + id

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case event: Any => updateState(event)
  }

  override def receiveCommand: Receive = empty()

  // state definitions:
  def empty(): Receive = LoggingReceive {
    case event: ItemAdded => persist(event){ event =>
      updateState(event)
      customer ! Content(cart)
    }
  }

  def nonEmpty(): Receive = LoggingReceive {
    case event: ItemAdded => persist(event)(event => updateState(event))
    case event: ItemRemoved => persist(event) { event =>
      updateState(event)
      if (cart.items.isEmpty) customer ! CustomerActor.CartEmpty
    }
    case CustomerActor.StartCheckout => persist(CustomerActor.StartCheckout) { event =>
      updateState(event)
      val checkout = context.actorOf(Props(new CheckoutActor(Random.nextLong(), customer)), "Checkout")
      customer ! CheckoutStarted(checkout, System.currentTimeMillis())
      checkout ! CheckoutStarted(context.parent, System.currentTimeMillis())
    }
    case CartTimerExpired => persist(CartTimerExpired) { event =>
      updateState(event)
      customer ! CustomerActor.CartEmpty
    }
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => persist(CheckoutClosed) { event =>
      println(cart)
      updateState(event)
      customer ! CustomerActor.CartEmpty
    }
    case CheckoutCancelled => persist(CheckoutCancelled)(event => updateState(event))
  }

  private def updateState(event: Any): Unit = {
    event match {
      case ItemAdded(item, date) =>
        if (cart.items.isEmpty) context become nonEmpty
        cart = cart.addItem(item)
        startTimer(date)
      case ItemRemoved(item, date) =>
        cart = cart.removeItem(item)
        if (cart.items.nonEmpty) startTimer(date)
        else {
          timers.cancel(cartTimer)
          context become empty
        }
      case CustomerActor.StartCheckout =>
        timers.cancel(cartTimer)
        context become inCheckout
      case CartTimerExpired =>
        cart = Cart.empty
        context become empty
      case CheckoutClosed =>
        cart = Cart.empty
        context become empty
      case CheckoutCancelled(date) =>
        startTimer(date)
        context become nonEmpty
    }
  }

  private def startTimer(startTime: Long): Unit = {
    val timeLeft = (startTime + cartTimeout.toMillis) - System.currentTimeMillis()
    if (timeLeft > 0) timers.startSingleTimer(cartTimer, CartTimerExpired, FiniteDuration(timeLeft, TimeUnit.MILLISECONDS))
    else updateState(CartTimerExpired)
  }
}
