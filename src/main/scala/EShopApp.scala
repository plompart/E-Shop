import actors.CustomerActor
import actors.CustomerActor.{Init, Init2}
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object EShopApp extends App {
  val actorSystem = ActorSystem("eshop")
  val customer = actorSystem.actorOf(Props[CustomerActor],"Customer")
  customer ! Init

  Thread.sleep(5 * 1000)
  actorSystem.terminate()
  Thread.sleep(5 * 1000)

  val actorSystem2 = ActorSystem("EShop")
  val customer2 = actorSystem2.actorOf(Props[CustomerActor],"Customer")
  //customer2 ! Init
  customer2 ! Init2
  Await.result(actorSystem2.whenTerminated,Duration.Inf)
}