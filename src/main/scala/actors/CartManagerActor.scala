package actors

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Timers}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration._
import scala.language.postfixOps

object CartManagerActor  {
  case class ItemRemoved(item: Item, date: Long)
  case class ItemAdded(item: Item, date: Long)
  case class CheckoutStarted(checkout: ActorRef, date: Long)
  case class CheckoutCancelled(date: Long = System.currentTimeMillis())
  case class CheckoutClosed()
  case class CartTimerExpired()

  case class Checkout(checkout: ActorRef)
}

class CartManagerActor(id: Long, customer: ActorRef, var cart: Cart) extends PersistentActor with Timers{
  import CartManagerActor._

  //def this() = this(1, context.parent, Cart.empty)
  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(5, TimeUnit.MINUTES)

  override def persistenceId: String = "CartManagerActor:" + id

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case event: Any => updateState(event)
  }

  override def receiveCommand: Receive = empty

  def empty(): Receive = LoggingReceive {
    case event: ItemAdded =>
      persist(event)(event => updateState(event))
  }

  def nonEmpty(): Receive = LoggingReceive {
    case event: ItemAdded =>
      persist(event)(event => updateState(event))
    case event: ItemRemoved =>
      persist(event)(event => {
        updateState(event)
        if (cart.items.isEmpty) sender ! CustomerActor.CartEmpty
      })
    case CustomerActor.StartCheckout =>
      persist(CustomerActor.StartCheckout)(event => {
        updateState(event)
        val checkout: ActorRef = context.actorOf(CheckoutActor.props(self, customer), "checkout")
        checkout ! CheckoutActor.CheckoutStarted()
        customer ! CheckoutStarted(checkout, System.currentTimeMillis())
      })
    case CartTimerExpired =>
      persist(CartTimerExpired) { event =>
        updateState(event)
        sender ! CustomerActor.CartEmpty
      }
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed =>
      persist(CheckoutClosed) { event =>
        updateState(event)
        customer ! CustomerActor.CartEmpty
      }
    case CheckoutCancelled =>
      persist(CheckoutCancelled)(event => {
        sender ! CartManagerActor.CheckoutStarted
        updateState(event)
      })
  }

  private def updateState(event: Any): Unit = {
    event match {
      case ItemAdded(item, date) =>
        if(cart.items.isEmpty) context become nonEmpty
        cart = cart.addItem(item)
        handleTimer(date)
      case ItemRemoved(item, date) =>
        cart = cart.removeItem(item)
        if(cart.items.nonEmpty){
          handleTimer(date)
        }else {
          context become empty
        }
      case CustomerActor.StartCheckout =>
        timers.cancel(cartTimer)
        context become inCheckout
      case CheckoutCancelled(date) =>
        handleTimer(date)
        context become nonEmpty
      case CheckoutClosed | CartTimerExpired =>
        context become empty
        cart = Cart.empty
    }
  }

  private def handleTimer(time: Long): Unit = {
    timers.cancel(cartTimer)
    val currentTime = System.currentTimeMillis()
    val timeoutTime = time + FiniteDuration(10, TimeUnit.SECONDS).toMillis
    if (timeoutTime <= currentTime) updateState(CartTimerExpired)
    else timers.startSingleTimer(cartTimer, CartTimerExpired, (timeoutTime - currentTime) millis)
  }

}
