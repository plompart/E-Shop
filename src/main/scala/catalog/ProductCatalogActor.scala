package catalog

import actors.Item
import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

object ProductCatalogActor {
  case class FindItems(query: String)
  case class Result(items: List[Item], client: ActorRef)
  case class NewItem(item: Item)
  case class ItemLine(line: String)

  case class Query(query: String)
  case class QueryResult(items: List[Item])

  case class FindProducts(query: String)
}

class ProductCatalogActor extends Actor {
  import ProductCatalogActor._

  private val itemStore = context.actorOf(Props[ItemStoreManagerActor], "itemStore")

  override def receive: Receive = LoggingReceive {
    case FindProducts(query) => itemStore forward FindProducts(query)
  }

}
