package services

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.http.scaladsl.model._
import services.PaymentServerApp.PayPalPaymentData

import scala.util.Random

class PaypalServiceActor extends Actor {
  val random: Random.type = Random

  override def receive: Receive = LoggingReceive {
    case data: PayPalPaymentData =>
      sender ! StatusCodes.OK
      context.stop(self)
  }

  def isNumberOfLength(number: String, length: Int): Boolean = {
    try {
      number.toInt
    } catch {
      case nfe: NumberFormatException => return false
    }
    number.length == length
  }

  def shouldFail(): Boolean = {
    random.nextInt(100) > 49
  }

  def returnRandomError(): StatusCode = {
    if (random.nextInt(100) > 49) {
      PaymentErrors.clientErrors(random.nextInt(PaymentErrors.clientErrors.size))
    } else {
      PaymentErrors.serverErrors(random.nextInt(PaymentErrors.serverErrors.size))
    }
  }
}
