package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

object PaymentServiceActor {
  case class DoPayment()
  case class PaymentConfirmed()

  def props(checkout: ActorRef): Props = Props(new PaymentServiceActor(checkout))
}

class PaymentServiceActor(checkout: ActorRef) extends Actor {
  import PaymentServiceActor._

  def receive = LoggingReceive {
    case DoPayment =>
      sender ! PaymentConfirmed
      checkout ! CheckoutActor.PaymentReceived
      context stop self
  }
}
