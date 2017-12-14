package catalog

import akka.actor.{Actor}
import akka.event.LoggingReceive

class ItemStoreManagerActor extends Actor{
  import ProductCatalogActor._

  private val itemStore = new ItemStore()

  override def receive: Receive = LoggingReceive {
    case FindProducts(query) => sender ! itemStore.getBestMatch(query)
  }

}