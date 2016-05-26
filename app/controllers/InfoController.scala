package controllers

import javax.inject.{Inject, Singleton}

import actor.UserActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.Info
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

@Singleton
class InfoController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def info = Action {
    implicit val infoWrites = Json.writes[Info]
    Ok(Json.toJson(Info()))
  }

  def info2 = Action(parse.json) { implicit request =>
    implicit val infoReads = Json.reads[Info]
    Ok("Got request [" + request + "] ->" + request.body.as[Info])
  }

  def websock = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => UserActor.props(out))
  }
}
