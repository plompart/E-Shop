package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

object PaymentServiceActor {
  case class DoPayment()
  case class PaymentConfirmed()
}

class PaymentServiceActor(customer: ActorRef) extends Actor {
  import PaymentServiceActor._

  private val checkout: ActorRef = context.parent

  def receive = LoggingReceive {
    case DoPayment =>
      customer ! PaymentConfirmed
      checkout ! CheckoutActor.PaymentReceived
      context stop self
  }
}
