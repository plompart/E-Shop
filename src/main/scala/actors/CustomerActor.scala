package actors

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

object CustomerActor  {
  case class StartCheckout()
  case class Init()
  case class PaymentServiceStarted(paymentService: ActorRef)
  case class CartEmpty()
}

class CustomerActor extends Actor {
  import CustomerActor._

  def receive: Receive = InCart()

  def InCart(): Receive = LoggingReceive {
    case Init =>
      val cart: ActorRef = context.actorOf(CartActor.props(self), "cart")
      cart ! CartActor.ItemAdded
      cart ! CartActor.ItemRemoved
      cart ! CartActor.ItemAdded
      cart ! StartCheckout
      context become inCheckout
    case CartEmpty =>
      context.system.terminate
  }

  def inCheckout(): Receive = LoggingReceive {
    case CartActor.CheckoutClosed() =>
      context become InCart
    case CartActor.CheckoutStarted(checkout) =>
      checkout ! CheckoutActor.DeliveryMethodSelected
      checkout ! CheckoutActor.PaymentSelected
      context become inPayment
  }

  def inPayment(): Receive = LoggingReceive {
    case PaymentServiceStarted(service) =>
      service ! PaymentServiceActor.DoPayment
    case PaymentServiceActor.PaymentConfirmed =>
      context become inCheckout
  }
}
