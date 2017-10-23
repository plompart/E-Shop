package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

class EShopActor extends Actor {
  val cart: ActorRef = context.actorOf(Props[CartActor], "cart")

  def receive = LoggingReceive {
    case "Init" =>
      cart ! CartActor.ItemAdded
      cart ! CartActor.ItemRemoved
      cart ! CartActor.ItemAdded
      //Thread.sleep(5000)
      cart ! CartActor.ItemAdded
      cart ! CartActor.ItemAdded
      cart ! CartActor.ItemRemoved
      cart ! CartActor.CheckoutStarted()
    case CartActor.Checkout(checkout: ActorRef) =>
      checkout ! CheckoutActor.DeliveryMethodSelected
      //Thread.sleep(5000)
      checkout ! CheckoutActor.PaymentSelected
      //Thread.sleep(5000)
      checkout ! CheckoutActor.PaymentReceived
    case CartActor.CheckoutClosed =>
      cart ! CartActor.CheckoutClosed
  }
}