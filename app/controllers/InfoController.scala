package controllers

import javax.inject.Singleton

import models.Info
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

@Singleton
class InfoController extends Controller {

  def info = Action {
    implicit val infoWrites = Json.writes[Info]
    Ok(Json.toJson(Info()))
  }

  def info2 = Action(parse.json) { implicit request =>
    implicit val infoReads = Json.reads[Info]
    Ok("Got request [" + request + "] ->" + request.body.as[Info])
  }
}
