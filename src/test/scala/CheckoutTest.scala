
import actors.CheckoutActor
import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.{FeatureSpecLike, GivenWhenThen}

import scala.concurrent.duration._

class CheckoutTest extends FeatureSpecLike with GivenWhenThen{
  import actors.CheckoutActor._
  import actors.CartManagerActor._

  val actorSystemName = "EshopPersistenceTest"

    scenario("System was terminated just after getting DeliveryMethodSelected, timer is still working, with refreshed time") {
      Given("ActorSystem and checkout actor")
      val currentTime = System.nanoTime()
      val actorSystem = ActorSystem(actorSystemName)
      val cartProbe = TestProbe()(actorSystem)
      val customerTestProbe = TestProbe()(actorSystem)
      val checkoutActor = cartProbe.childActorOf(Props(classOf[CheckoutActor], currentTime, customerTestProbe.testActor))

      When("System terminates after getting DeliveryMethodSelected after some time (5 seconds)")
      checkoutActor.tell(CheckoutStarted(customerTestProbe.testActor, System.currentTimeMillis()), customerTestProbe.testActor)
      checkoutActor.tell(DeliveryMethodSelected, customerTestProbe.testActor)
      customerTestProbe.expectNoMessage(5 seconds)
      actorSystem.terminate()

      Then("Checkout will have lower timer time (6 seconds)")
      val restartedActorSystem = ActorSystem(actorSystemName)
      val restartedCartProbe = TestProbe()(restartedActorSystem)
      val restartedCustomerTestProbe = TestProbe()(restartedActorSystem)
      val restartedCheckoutActor = restartedCartProbe.childActorOf(Props(classOf[CheckoutActor], currentTime, restartedCustomerTestProbe.testActor))

      restartedCustomerTestProbe.expectMsg(6 seconds, CheckoutCancelled)
      restartedActorSystem.terminate()
    }

}
