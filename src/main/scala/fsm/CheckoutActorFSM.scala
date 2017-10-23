package fsm

import akka.actor.{Actor, ActorRef, FSM, Props}

import scala.concurrent.duration._
import scala.language.postfixOps

object CheckoutActorFSM {
  sealed trait FSMCheckoutMessage
  final case class Cancelled() extends FSMCheckoutMessage
  final case class DeliveryMethodSelected() extends FSMCheckoutMessage
  final case class PaymentSelected() extends FSMCheckoutMessage
  final case class PaymentReceived() extends FSMCheckoutMessage
  final case class CheckoutTimer() extends FSMCheckoutMessage
  final case class PaymentTimer() extends FSMCheckoutMessage

  sealed trait FSMCheckoutState
  case object SelectingDelivery extends FSMCheckoutState
  case object SelectingPaymentMethod extends FSMCheckoutState
  case object ProcessingPayment extends FSMCheckoutState

  def props(cart: ActorRef): Props = Props(new CheckoutActorFSM(cart))

  final case class Checkout()
}



class CheckoutActorFSM(cart: ActorRef) extends Actor with FSM[CheckoutActorFSM.FSMCheckoutState, CheckoutActorFSM.Checkout]  {
  import CheckoutActorFSM._

  startWith(SelectingDelivery, Checkout())


  when(SelectingDelivery) {
    case Event(DeliveryMethodSelected, _) =>
      goto(SelectingPaymentMethod)
    case Event(Cancelled, Checkout()) =>
      cart ! CartActorFSM.CheckoutCancelled
      stop
    case Event(CheckoutTimer, Checkout()) =>
      cart ! CartActorFSM.CheckoutCancelled
      stop
  }

  when(SelectingPaymentMethod) {
    case Event(PaymentSelected, _) =>
      goto(ProcessingPayment)
    case Event(CheckoutTimer, Checkout()) =>
      cart ! CartActorFSM.CheckoutCancelled
      stop
  }

  when(ProcessingPayment) {
    case Event(PaymentReceived, Checkout()) =>
      cart ! CartActorFSM.CheckoutClosed
      stop
    case Event(PaymentTimer, Checkout()) =>
      cart ! CartActorFSM.CheckoutCancelled
      stop
  }

  onTransition {
    case _ -> SelectingDelivery => setTimer("CheckoutTimer", CheckoutTimer, 3.seconds, repeat = false)
    case SelectingPaymentMethod -> ProcessingPayment =>
      cancelTimer("CheckoutTimer")
      setTimer("PaymentTimer", PaymentTimer, 3.seconds, repeat = false)
  }

  initialize()
}
