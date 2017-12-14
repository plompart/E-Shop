package actors.payment

import actors.PaymentServiceActor.PaymentSucceeded
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.concurrent.ExecutionContext.Implicits.global

class CardClientActor(cardNumber: String, expirationDate: String, owner: String, cvv: String) extends Actor with ActorLogging  {
  override def preStart(): Unit = {
    System.out.println("CreditCard actor created.")
    val formData = FormData(Map("cardNumber" -> cardNumber, "expirationDate" -> expirationDate, "owner" -> owner, "cvv" -> cvv))
    http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/creditCard", entity = formData.toEntity))
      .pipeTo(self)

  }

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive: Receive = LoggingReceive {
    case HttpResponse(StatusCodes.OK, _, _, _) =>
      log.info(StatusCodes.OK.toString())
      context.parent ! PaymentSucceeded
    case HttpResponse(error: StatusCodes.ClientError, _, _, _) =>
      log.info(error.toString())
      throw new IllegalRequestException(ErrorInfo(error.defaultMessage, error.reason), error)
    case HttpResponse(error: StatusCodes.ServerError, _, _, _) =>
      log.info(error.toString())
      throw new IllegalResponseException(ErrorInfo(error.defaultMessage, error.reason))
    case Failure(exception) => throw exception
  }
}
