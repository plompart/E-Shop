package actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.language.postfixOps

object CartActor  {
  case class ItemRemoved()
  case class ItemAdded()
  case class CheckoutStarted(checkout: ActorRef)
  case class CheckoutCancelled()
  case class CheckoutClosed()
  case class CartTimerExpired()

  case class Checkout(checkout: ActorRef)

  def props(customer: ActorRef): Props = Props(new CartActor(customer))
}

class CartActor(customer: ActorRef) extends Actor with Timers{
  import CartActor._

  var itemCount = BigInt(0)

  def receive: Receive = empty

  def empty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      timers.startSingleTimer(CartTimerExpired, CartTimerExpired, 3.seconds)
      context become nonEmpty
  }

  def nonEmpty(): Receive = LoggingReceive {
    case ItemAdded =>
      timers.cancel(CartTimerExpired)
      timers.startSingleTimer(CartTimerExpired, CartTimerExpired, 3.seconds)
      itemCount += 1
    case ItemRemoved if itemCount > 1 =>
      timers.cancel(CartTimerExpired)
      timers.startSingleTimer(CartTimerExpired, CartTimerExpired, 3.seconds)
      itemCount -= 1
    case ItemRemoved if itemCount == 1 =>
      timers.cancel(CartTimerExpired)
      itemCount -= 1
      context become empty
    case CustomerActor.StartCheckout =>
      timers.cancel(CartTimerExpired)
      val checkout: ActorRef = context.actorOf(CheckoutActor.props(self, customer), "checkout")
      checkout ! CheckoutActor.CheckoutStarted()
      customer ! CheckoutStarted(checkout)
      context become inCheckout
    case CartTimerExpired =>
      itemCount = 0
      context become empty
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed =>
      itemCount = 0
      println("CheckoutClosed")
      customer ! CustomerActor.CartEmpty
      context become empty
    case CheckoutCancelled =>
      sender ! CartActor.CheckoutStarted
      timers.startSingleTimer(CartTimerExpired, CartTimerExpired, 3.seconds)
      context become nonEmpty
  }

}
