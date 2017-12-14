package actors

import java.net.URI

import actors.CartManagerActor.CheckoutClosed
import actors.CheckoutActor.{DeliveryMethodSelected, PaymentSelected}
import actors.PaymentServiceActor._
import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import catalog.ProductCatalogActor.FindProducts

object CustomerActor {
  case class StartCheckout()
  case class Init()
  case class Init2()
  case class PaymentServiceStarted(paymentService: ActorRef)
  case class CartEmpty()
  case class Checkout()
}

class CustomerActor extends Actor {
  import CustomerActor._

  val cart: ActorRef = context.actorOf(Props(new CartManagerActor(1, Cart.empty, 1)), "CartManager")

  override def receive = LoggingReceive{
    case query: String =>
      val productStore = context.actorSelection("akka.tcp://ProductCatalog@127.0.0.1:25552/user/catalog")
      productStore ! FindProducts(query)
    case items: List[Item] => items.foreach(i => {
      System.out.println("\t" + i)
    })
    case Init =>
      cart ! CartManagerActor.ItemAdded(Item(new URI("http://item1.pl"), "item1", 10, 1), System.currentTimeMillis())
      cart ! CartManagerActor.ItemRemoved(Item(new URI("http://item2.pl"), "item2", 10, 1), System.currentTimeMillis())
      cart ! CartManagerActor.ItemAdded(Item(new URI("http://item3.pl"), "item3", 10, 1), System.currentTimeMillis())
    case Init2 =>
      Thread.sleep(3 * 1000)
      //cart ! CartManagerActor.ItemRemoved(Item(new URI("http://item3.pl"), "item3", 10, 1), System.currentTimeMillis())
      cart ! CartManagerActor.ItemAdded(Item(new URI("http://item3.pl"), "item3", 10, 1), System.currentTimeMillis())
      cart ! StartCheckout
    case CartManagerActor.CheckoutStarted(checkout, _) =>
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected(System.currentTimeMillis())
    case PaymentServiceStarted(paymentService) =>
      paymentService ! DoPayment(PayU("123456"))
      //paymentService ! DoPayment(CreditCard("1234567812345678", "07/95", "John Doe", "777"))
      //paymentService ! DoPayment(PayPal("login", "password"))
    case PaymentConfirmed =>
      println(PaymentConfirmed.toString)
    case CheckoutClosed =>
      println(CheckoutClosed.toString)
    case CartEmpty =>
      println(CartEmpty.toString)
    case CartManagerActor.Content(cart) =>
        println(cart.toString)
    }
}
