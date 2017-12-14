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

class PayUClientActor(code: String) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    System.out.println("PayU actor created.")
    val formData = FormData(Map("code" -> code))
    http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/PayU", entity = formData.toEntity))
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
