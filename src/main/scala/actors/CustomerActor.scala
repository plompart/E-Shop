package actors

import java.net.URI

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

object CustomerActor {
  case class StartCheckout()
  case class Init()
  case class PaymentServiceStarted(paymentService: ActorRef)
  case class CartEmpty()
  case class Checkout()
}

class CustomerActor extends Actor {
  import CustomerActor._

  def receive: Receive = InCart()
  val cart: ActorRef = context.actorOf(Props(new CartManagerActor(1, self, Cart.empty)), "Cart")
  def InCart(): Receive = LoggingReceive {
    case Init =>
      cart ! CartManagerActor.ItemAdded(Item(new URI("http://item1.pl"), "item1", 10, 1), System.currentTimeMillis())
      cart ! CartManagerActor.ItemRemoved(Item(new URI("http://item2.pl"), "item2", 10, 1), System.currentTimeMillis())
      cart ! CartManagerActor.ItemAdded(Item(new URI("http://item3.pl"), "item3", 10, 1), System.currentTimeMillis())
    case CartEmpty =>
      context.system.terminate
    case Checkout =>
      cart ! StartCheckout
      context become inCheckout
  }

  def inCheckout(): Receive = LoggingReceive {
    case CartManagerActor.CheckoutClosed() =>
      context become InCart
    case CartManagerActor.CheckoutStarted(checkout, date) =>
      checkout ! CheckoutActor.DeliveryMethodSelected
      checkout ! CheckoutActor.PaymentSelected(System.currentTimeMillis())
      context become inPayment
  }

  def inPayment(): Receive = LoggingReceive {
    case PaymentServiceStarted(service) =>
      service ! PaymentServiceActor.DoPayment
    case PaymentServiceActor.PaymentConfirmed =>
      context become inCheckout
  }
}
