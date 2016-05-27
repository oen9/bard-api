package actor

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import models.{Payload, ToPublish}

class UserActor(out: ActorRef) extends Actor {

  val mediator = DistributedPubSub(context.system).mediator
  // subscribe to the topic named "content"
  mediator ! Subscribe("content", self)
  val publisher = context.system.actorSelection("/user/publisher")

  def receive = {
    case SubscribeAck(Subscribe("content", None, `self`)) ⇒ println("subscribing")
    case msg: Payload if msg.event == "publish" => publisher ! ToPublish(msg)
    case ToPublish(payload) => out ! payload
    case Payload(event, content) => out ! Payload("response", content)
  }
}

object UserActor {
  def props(out: ActorRef) = Props(new UserActor(out))
}
