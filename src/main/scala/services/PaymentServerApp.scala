package services

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import services.PaymentServerApp.{CardPaymentData, PayPalPaymentData, PayUPaymentData}

import scala.concurrent.duration.FiniteDuration
import scala.util.Success

object PaymentServerApp extends App {

  private val interface = "localhost"

  private val port = 8080

  private val server = new PaymentServerApp()

  server.startServer(interface, port)

  case class PayUPaymentData(code: String)
  case class CardPaymentData(cardNumber: String, expirationDate: String, owner: String, cvv: String)
  case class PayPalPaymentData(login: String, password: String)

}

class PaymentServerApp extends HttpApp {
  private val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("PaymentServer", config.getConfig("payment").withFallback(config))

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  override protected def routes: Route = {
    pathEndOrSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<p>PaymentServer</p>"))
      }
    } ~
      pathPrefix("PayU") {
        pathEndOrSingleSlash {
          post {
            formField('code).as(PayUPaymentData) {
              paymentData =>
                val blikHandler = system.actorOf(Props[PayUServiceActor])
                onComplete(blikHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      } ~
      pathPrefix("creditCard") {
        pathEndOrSingleSlash {
          post {
            formField('cardNumber, 'expirationDate, 'owner, 'cvv).as(CardPaymentData) {
              paymentData =>
                val cardHandler = system.actorOf(Props[CardServiceActor])
                onComplete(cardHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      } ~
      pathPrefix("Paypal") {
        pathEndOrSingleSlash {
          post {
            formField('login, 'password).as(PayPalPaymentData) {
              paymentData =>
                val paypalHandler = system.actorOf(Props[PaypalServiceActor])
                onComplete(paypalHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      }
  }
}

