import actors.{CustomerActor}
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object EShopApp extends App {
  val system = ActorSystem("EShopApp")
  val customer = system.actorOf(Props[CustomerActor], "customer")

  customer ! CustomerActor.Init

  Await.result(system.whenTerminated, Duration.Inf)
}