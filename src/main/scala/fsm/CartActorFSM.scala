package fsm

import akka.actor.{Actor, ActorRef, FSM}
import scala.concurrent.duration._
import scala.language.postfixOps

object CartActorFSM {
  sealed trait FSMActorMessage
  final case class ItemAdded() extends FSMActorMessage
  final case class ItemRemoved() extends FSMActorMessage
  final case class CheckoutCancelled() extends FSMActorMessage
  final case class CheckoutClosed() extends FSMActorMessage
  final case class CheckoutStarted() extends FSMActorMessage
  final case class CartTimerExpired() extends FSMActorMessage

  final case class Checkout(checkout: ActorRef) extends FSMActorMessage

  sealed trait FSMActorState
  case object Empty extends FSMActorState
  case object NonEmpty extends FSMActorState
  case object InCheckout extends FSMActorState

  final case class Cart(itemCounter: BigInt)
}

class CartActorFSM extends Actor with FSM[CartActorFSM.FSMActorState, CartActorFSM.Cart] {
  import CartActorFSM._

  startWith(Empty, Cart(BigInt(0)))

  when(Empty) {
    case Event(ItemAdded, _) =>
      goto(NonEmpty) using Cart(1)
  }

  when(NonEmpty) {
    case Event(ItemAdded, Cart(itemCounter)) =>
      stay using Cart(itemCounter + 1)
    case Event(ItemRemoved, Cart(itemCounter)) if itemCounter > 1 =>
      stay using Cart(itemCounter - 1)
    case Event(ItemRemoved, Cart(itemCounter)) if itemCounter == 1 =>
      goto(Empty) using Cart(itemCounter - 1)
    case Event(CheckoutStarted, _) =>
      val checkout: ActorRef = context.actorOf(CheckoutActorFSM.props(self), "cart")
      sender() ! Checkout(checkout)
      goto(InCheckout)
    case Event(CartTimerExpired, _) =>
      goto(Empty) using Cart(0)
  }

  when(InCheckout) {
    case Event(CheckoutCancelled, _) =>
      goto (NonEmpty)
    case Event(CheckoutClosed, _) =>
      println("CheckoutClosed")
      goto (NonEmpty) using Cart(0)
  }

  onTransition {
    case _ -> NonEmpty => setTimer("CartTimerExpired", CartTimerExpired, 3.seconds, repeat = false)
    case NonEmpty -> _ => cancelTimer("CartTimerExpired")
  }

  initialize()
}
