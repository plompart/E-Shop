package catalog

import actors.Item

class ItemStore {
  val file = "C:\\Users\\Patryk\\Desktop\\Studia\\VII semestr\\Scala\\E-Shop\\src\\main\\resources\\query_result"

  private val items = ProductParser.parseProducts(file)

  def getBestMatch(query: String): List[Item] = {
    val keywords = query.split(" ")
    items.map(i => (i, countKeywords(i, keywords)))
      .sortBy(_._2)
      .reverse
      .take(10)
      .map(i => i._1)
  }

  private def countKeywords(item: Item, keywords: Array[String]): Int = {
    item.name.split(" ")
      .map(s => if (keywords.contains(s)) 1 else 0)
      .sum
  }

}