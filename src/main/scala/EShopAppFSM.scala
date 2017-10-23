import fsm.EShopActorFSM
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object EShopAppFSM extends App {
  val system = ActorSystem("EShopAppFSM")
  val mainActor = system.actorOf(Props[EShopActorFSM], "mainActor")

  mainActor ! "Init"

  Await.result(system.whenTerminated, Duration.Inf)
}