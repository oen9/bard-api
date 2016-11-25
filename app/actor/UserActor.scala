package actor

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import models.{ToPublish, _}
import play.api.libs.json.Json
import models.Formats._
import models.MsgTypes._

class UserActor(out: ActorRef) extends Actor {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("content", self)

  val publisher: ActorSelection = context.system.actorSelection("/user/publisher")
  val playlist: ActorSelection = context.system.actorSelection("/user/playlist")

  playlist ! GetPlaylist

  def receive = {
    case SubscribeAck(Subscribe("content", None, `self`)) ⇒ println("subscribing")

    case msg: Payload if msg.event == PUBLISH =>
      publisher ! ToPublish(msg)
    case ToPublish(payload) =>
      out ! payload

    case msg: Payload if msg.event == PLAY || msg.event == STOP =>
      playlist ! msg
    case msg: Payload if msg.event == NEXT =>
      playlist ! PlayNext
    case msg: Payload if msg.event == ADD =>
      playlist ! AddToPlaylist(msg.content.get)
    case msg: Payload if msg.event == CLEAR =>
      playlist ! ClearPlaylist
      publisher ! ToPublish(msg)
    case msg: Playlist =>
      out ! Payload(PLAYLIST, Some(Json.toJson(msg).toString()))

    case Payload(event, content) =>
      out ! Payload("response", content)
  }
}

object UserActor {
  def props(out: ActorRef) = Props(new UserActor(out))
}
