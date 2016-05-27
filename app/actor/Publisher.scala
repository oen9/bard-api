package actor

import akka.actor.Actor
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import models.ToPublish

class Publisher extends Actor {
  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case msg: ToPublish => mediator ! Publish("content", msg)
    case in: String ⇒ mediator ! Publish("content", in)
  }
}
