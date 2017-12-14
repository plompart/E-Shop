import actors.CustomerActor.Init2
import actors.{CartManagerActor, CustomerActor, Item}
import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import catalog.ProductCatalogActor

import scala.language.postfixOps

object EShopApp extends App {
  implicit val timeout = Timeout(60 seconds)
  val productString = "ProductCatalog"
  val catalogString = "catalog"
  val config = ConfigFactory.load()

  val productSystem = ActorSystem(productString, config.getConfig(catalogString).withFallback(config))
  val shopSystem = ActorSystem("ShopSystem", config.getConfig("eShop").withFallback(config))

  val catalog = productSystem.actorOf(Props[ProductCatalogActor], catalogString)
  val customer = shopSystem.actorOf(Props[CustomerActor],"Customer")

  val futureAnswer = customer ? "Dutch Cocoa"

  customer ! Init2

  val result = Await.result(futureAnswer, timeout.duration).asInstanceOf[Item]

  Await.result(shopSystem.whenTerminated,Duration.Inf)
}