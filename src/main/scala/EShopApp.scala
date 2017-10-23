import actors.EShopActor
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object EShopApp extends App {
  val system = ActorSystem("EShopApp")
  val mainActor = system.actorOf(Props[EShopActor], "mainActor")

  mainActor ! "Init"

  Await.result(system.whenTerminated, Duration.Inf)
}