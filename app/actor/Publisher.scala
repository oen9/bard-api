package actor

import akka.actor.Actor
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish

class Publisher extends Actor {
  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case in: String ⇒
      mediator ! Publish("content", in)
  }
}
