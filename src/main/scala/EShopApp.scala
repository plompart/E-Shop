import actors.{CustomerActor}
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object EShopApp extends App {
  val system = ActorSystem("EShopApp")
  val customer = system.actorOf(Props[CustomerActor], "customer")

  customer ! CustomerActor.Init

  Thread.sleep(5 * 1000)
  system.terminate()
  Thread.sleep(5 * 1000)

  val system2 = ActorSystem("EShopApp")
  val customer2 = system2.actorOf(Props[CustomerActor], "customer")
  customer2 ! CustomerActor.Checkout

  Await.result(system.whenTerminated, Duration.Inf)
}