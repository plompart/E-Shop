package fsm

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

class EShopActorFSM extends Actor {
  val cart: ActorRef = context.actorOf(Props[CartActorFSM], "cartFSM")

  def receive = LoggingReceive {
    case "Init" =>
      cart ! CartActorFSM.ItemAdded
      cart ! CartActorFSM.ItemRemoved
      cart ! CartActorFSM.ItemAdded
      //Thread.sleep(5000)
      cart ! CartActorFSM.ItemAdded
      cart ! CartActorFSM.ItemAdded
      cart ! CartActorFSM.ItemRemoved
      cart ! CartActorFSM.CheckoutStarted
    case CartActorFSM.Checkout(checkout: ActorRef) =>
      checkout ! CheckoutActorFSM.DeliveryMethodSelected
      //Thread.sleep(5000)
      checkout ! CheckoutActorFSM.PaymentSelected
      //Thread.sleep(5000)
      checkout ! CheckoutActorFSM.PaymentReceived
    case CartActorFSM.CheckoutClosed =>
      cart ! CartActorFSM.CheckoutClosed
  }
}