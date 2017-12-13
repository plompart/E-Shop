import java.net.URI

import actors.{Cart, CartManagerActor, Item}
import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe

import org.scalatest.{FeatureSpecLike, GivenWhenThen}

import scala.concurrent.duration._

class CartManagerTest extends FeatureSpecLike with GivenWhenThen{
  import actors.CustomerActor._
  import actors.CartManagerActor._

  val actorSystemName = "EshopPersistenceTest"

  feature("CartManager is persisted"){
    scenario("Timers are persisted"){
      Given("ActorSystem and CartManager")
      val currentTime = System.nanoTime()

      val actorSystem = ActorSystem(actorSystemName)
      val customer = TestProbe()(actorSystem)
      val cart = customer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))

      When("actor receives item, becomes non-empty and system is terminated after 5 seconds")
      val item = Item(new URI("http://sdssad3.pl"),"cookie",10,1)
      cart.tell(ItemAdded(item, System.currentTimeMillis()),customer.testActor)
      customer.expectMsgClass(classOf[Content])
      customer.expectNoMessage(5 seconds)
      actorSystem.terminate()

      Then("After restart timeout happens after 6 seconds instead of 10")
      val restartedActorSystem = ActorSystem(actorSystemName)
      val restartedCustomer = TestProbe()(restartedActorSystem)
      val restartedCart = restartedCustomer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))
      restartedCustomer.expectMsg(6 seconds, CartEmpty)
      restartedActorSystem.terminate()
    }

    scenario("Shopping cart is restored along with manager"){
      Given("ActorSystem and CartManager")
      val currentTime = System.nanoTime()

      val actorSystem = ActorSystem(actorSystemName)
      val customer = TestProbe()(actorSystem)
      val cart = customer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))

      When("actor receives item, becomes non-empty and system is terminated after 5 seconds")
      val item = Item(new URI("http://sdssad3.pl"),"cookie",10,1)
      cart.tell(ItemAdded(item, System.currentTimeMillis()),customer.testActor)
      customer.expectMsgClass(classOf[Content])
      customer.expectNoMessage(5 seconds)
      actorSystem.terminate()

      Then("After restart and adding another item, cart got 2 items")
      val anotherItem = Item(new URI("http://sdssad32.pl"),"cookie",10,1)
      val restartedActorSystem = ActorSystem(actorSystemName)
      val restartedCustomer = TestProbe()(restartedActorSystem)
      val restartedCart = restartedCustomer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))

      restartedCart.tell(ItemAdded(anotherItem, System.currentTimeMillis()),restartedCustomer.testActor)
      val content = restartedCustomer.expectMsgType[Content]
      assert(content.Cart.items.size == 2)
      restartedActorSystem.terminate()
    }


    scenario("CartManager that was in checkout, stays in checkout after restart"){
      Given("ActorSystem and CartManager")
      val currentTime = System.nanoTime()

      val actorSystem = ActorSystem(actorSystemName)
      val customer = TestProbe()(actorSystem)
      val cart = customer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))

      When("actor receives item and goes to incheckout state, then system is terminated after 5 seconds")
      val item = Item(new URI("http://sdssad3.pl"),"cookie",10,1)
      cart.tell(ItemAdded(item, System.currentTimeMillis()), customer.testActor)
      customer.expectMsgClass(classOf[Content])
      cart.tell(StartCheckout, customer.testActor)
      customer.expectMsgClass(classOf[CheckoutStarted])
      customer.expectNoMessage(5 seconds)
      actorSystem.terminate()

      Then("After restart Cart Manager doesnt response to ItemAdded")
      val anotherItem = Item(new URI("http://sdssad32.pl"),"cookie",10,1)
      val restartedActorSystem = ActorSystem(actorSystemName)
      val restartedCustomer = TestProbe()(restartedActorSystem)
      val restartedCart = restartedCustomer.childActorOf(Props(classOf[CartManagerActor], currentTime, Cart.empty, currentTime))

      restartedCart.tell(ItemAdded(anotherItem, System.currentTimeMillis()),restartedCustomer.testActor)
      restartedCart.tell(ItemAdded(anotherItem, System.currentTimeMillis()),restartedCustomer.testActor)
      restartedCart.tell(ItemAdded(anotherItem, System.currentTimeMillis()),restartedCustomer.testActor)
      restartedCustomer.expectNoMessage(6 seconds)
      restartedActorSystem.terminate()
    }
  }
}
