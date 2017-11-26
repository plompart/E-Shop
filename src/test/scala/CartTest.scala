import actors.{CartManagerActor, CustomerActor}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class CartTest extends TestKit(ActorSystem("CartTest")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

//  import actors.CartManagerActor._
//
//  var cart: TestActorRef[CartManagerActor] = _
//
//  override def beforeAll(): Unit = {
//    cart = TestActorRef(new CartManagerActor(self))
//  }
//
//  override def afterAll(): Unit = {
//    system.terminate
//  }
//
//  "A Cart" must {
//    "start as empty" in {
//      assert(cart.underlyingActor.itemCount == 0)
//    }
//
//    "increment the value" in {
//      assert(cart.underlyingActor.itemCount == 0)
//      cart ! ItemAdded
//      assert(cart.underlyingActor.itemCount == 1)
//    }
//
//    "decrement the value" in {
//      assert(cart.underlyingActor.itemCount == 1)
//      cart ! ItemRemoved
//      assert(cart.underlyingActor.itemCount == 0)
//    }
//
//    "send back checkout" in {
//      cart ! ItemAdded
//      cart ! CustomerActor.StartCheckout
//
//      expectMsgType[CheckoutStarted]
//    }
//  }
}
