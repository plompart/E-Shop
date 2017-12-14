package catalog

import java.net.URI
import scala.io.Source
import actors.Item

object ProductParser {

  def parseProducts(file: String): List[Item] = {
    val items = Source.fromFile(file)
      .getLines()
      .drop(1)
      .map(_.replace("\"", ""))
      .map(parseProduct)
      .toList
    System.out.println("Finished parsing")
    items
  }

  private def parseProduct(line: String): Item = {
    val productInfo = line.split(",")
    val uri = URI.create(productInfo(0).trim)
    val name = parseName(productInfo)
    Item(uri, name, 10, 10)
  }

  private def parseName(productInfo: Array[String]): String = {
    val name = new StringBuilder(productInfo(1).trim)
    if (productInfo.length >= 3 && productInfo(2) != "NULL")
      name.append(" " + productInfo(2).trim)
    name.toString()
  }
}
