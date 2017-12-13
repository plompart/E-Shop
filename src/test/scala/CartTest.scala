import java.net.URI

import actors.{Cart, Item}
import org.scalatest.{FeatureSpecLike, GivenWhenThen}

class CartTest extends FeatureSpecLike with GivenWhenThen {
  feature("One can add and remove items into Cart") {
    scenario("Item was added to cart") {
      Given("an empty cart and an item")
      val cart = Cart.empty
      val item = Item(new URI("http://test/test"), "testItem", 10, 1)

      When("the item is added")
      val cartWithItem = cart.addItem(item)

      Then("cart has the item in it.")
      assert(cartWithItem.items.size == 1)
      assert(cartWithItem.items.values.forall(_ == item))
    }
    scenario("Item was removed from cart with one item of same id, with same quantity") {
      Given("an cart with one item")
      val item = Item(new URI("http://test/test"), "testItem", 10, 1)
      val cartWithItem = Cart.empty.addItem(item)

      When("the same item is removed, cart becomes empty")
      val emptyCart = cartWithItem.removeItem(item)

      Then("cart has the item in it.")
      assert(emptyCart.items.isEmpty)
    }
  }
}