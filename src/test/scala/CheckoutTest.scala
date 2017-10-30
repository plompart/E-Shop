import actors.CartActor
import actors.CheckoutActor.{DeliveryMethodSelected, PaymentReceived, PaymentSelected}
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class CheckoutTest extends TestKit(ActorSystem("CheckoutTest")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  import actors.CartActor._
  import actors.CustomerActor._

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Checkout" must {
    "send back CheckoutClosed to parent" in {
      val cart = TestActorRef(new CartActor(self))

      cart ! ItemAdded
      cart ! StartCheckout
      val msg: CheckoutStarted = expectMsgType[CheckoutStarted]

      msg.checkout ! DeliveryMethodSelected

      msg.checkout ! PaymentSelected
      expectMsgType[PaymentServiceStarted]

      msg.checkout ! PaymentReceived
      expectMsg(CheckoutClosed())

    }


  }
}
