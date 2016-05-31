package actor

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import models.{ToPublish, _}
import play.api.libs.json.Json
import models.Writes._

class UserActor(out: ActorRef) extends Actor {

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("content", self)

  val publisher = context.system.actorSelection("/user/publisher")
  val playlist = context.system.actorSelection("/user/playlist")

  playlist ! GetPlaylist

  def receive = {
    case SubscribeAck(Subscribe("content", None, `self`)) ⇒ println("subscribing")

    case msg: Payload if msg.event == "publish" => publisher ! ToPublish(msg)
    case ToPublish(payload) => out ! payload

    case msg: Payload if msg.event == "play" || msg.event == "stop" =>
      playlist ! msg
    case msg: Payload if msg.event == "add" =>
      playlist ! AddToPlaylist(msg.content.get)
    case msg: Payload if msg.event == "clear" =>
      playlist ! ClearPlaylist
      publisher ! ToPublish(msg)
    case msg: Playlist => out ! Payload("playlist", Some(Json.toJson(msg).toString()))

    case Payload(event, content) => out ! Payload("response", content)
  }
}

object UserActor {
  def props(out: ActorRef) = Props(new UserActor(out))
}
