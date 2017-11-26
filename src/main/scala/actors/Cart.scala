package actors

import java.net.URI

object Cart {
  val empty = Cart(Map.empty)
}

case class Item(id: URI, name: String, price: BigDecimal, count: Int)
case class Cart(items: Map[URI, Item]) {
  def addItem(it: Item): Cart = {
    val currentCount = if (items contains it.id) items(it.id).count else 0
    copy(items = items.updated(it.id, it.copy(count = currentCount + it.count)))
  }

  def removeItem(it: Item): Cart = {
    if(items contains it.id) {
      val oldItem = items(it.id)
      if (oldItem.count <= it.count) Cart(items - it.id)
      else Cart(items + (it.id -> it.copy(count = oldItem.count - it.count)))
    }else this
  }
}
