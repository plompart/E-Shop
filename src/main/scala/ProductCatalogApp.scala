import akka.actor.{ActorSystem, Props}
import catalog.ProductCatalogActor
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProductCatalogApp extends App {
  val systemName = "ProductCatalog"
  private val catalogString = "catalog"

  private val config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem(systemName, config.getConfig(catalogString).withFallback(config))

  val catalog = system.actorOf(Props[ProductCatalogActor], catalogString)
  Await.result(system.whenTerminated,Duration.Inf)
}