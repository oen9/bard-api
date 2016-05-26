package actor

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}

class UserActor(out: ActorRef) extends Actor {

  val publishPattern = """publish:(.*)""".r

  val mediator = DistributedPubSub(context.system).mediator
  // subscribe to the topic named "content"
  mediator ! Subscribe("content", self)
  val publisher = context.system.actorSelection("/user/publisher")

  def receive = {
    case publishPattern(msg) => publisher ! msg
    case msg: String => out ! ("I received your message: " + msg)
    case SubscribeAck(Subscribe("content", None, `self`)) ⇒ println("subscribing")
  }
}

object UserActor {
  def props(out: ActorRef) = Props(new UserActor(out))
}
