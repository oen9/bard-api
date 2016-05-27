package controllers

import javax.inject.Inject

import actor.UserActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.Payload
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc.{Controller, WebSocket}

class WebsockController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  implicit val payloadFormat = Json.format[Payload]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[Payload, Payload]

  def websock = WebSocket.accept[Payload, Payload] { request =>
    ActorFlow.actorRef(out => UserActor.props(out))
  }
}
