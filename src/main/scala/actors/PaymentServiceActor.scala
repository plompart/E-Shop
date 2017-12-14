package actors

import actors.CheckoutActor.PaymentReceived
import actors.payment.{CardClientActor, PayUClientActor, PaypalClientActor}
import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import akka.event.LoggingReceive
import akka.http.scaladsl.model.{IllegalRequestException, IllegalResponseException}
import akka.stream.StreamTcpException
import services.{CardServiceActor, PayUServiceActor, PaypalServiceActor}

object PaymentServiceActor {
  case class DoPayment(method: PaymentMethod)
  case class PaymentConfirmed()
  case class PaymentSucceeded()

  sealed trait PaymentMethod
  case class PayU(code: String) extends PaymentMethod
  case class CreditCard(cardNumber: String, expirationDate: String, owner: String, cvv: String) extends PaymentMethod
  case class PayPal(login: String, password: String) extends PaymentMethod
}

class PaymentServiceActor(customer: ActorRef) extends Actor {
  import PaymentServiceActor._

  private val checkout: ActorRef = context.parent

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = -1) {
    case _: IllegalRequestException => Restart
    case _: IllegalResponseException => Restart
    case _: StreamTcpException => Restart
    case _: Exception => Resume
  }

  def receive = LoggingReceive {
    case DoPayment(paymentType) =>
      paymentType match {
        case PayU(code) => context.actorOf(Props(classOf[PayUClientActor], code), "PayU")
        case CreditCard(cardNumber, expirationDate, owner, cvv) => context.actorOf(Props(classOf[CardClientActor], cardNumber, expirationDate, owner, cvv), "CreditCard")
        case PayPal(login, password) => context.actorOf(Props(classOf[PaypalClientActor], login, password), "PayPal")
      }
    case PaymentSucceeded =>
      customer ! PaymentConfirmed
      checkout ! PaymentReceived
  }
}
