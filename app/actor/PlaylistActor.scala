package actor

import javax.inject.Inject

import akka.actor.Actor
import models._
import models.youtube.Video
import org.psnively.scala.web.client.RestTemplate
import play.api.Configuration
import models.Writes._
import play.api.libs.json.Json

class PlaylistActor @Inject() (configuration: Configuration) extends Actor {

  val videoUrl = """https://www.googleapis.com/youtube/v3/videos?id={ytHash}&key={apiKey}&part=contentDetails,snippet"""
  val youtubeApiKey = configuration.getString("youtube.api.key").getOrElse("none")
  val restTemplate = new RestTemplate()

  var currentQueue: List[PlaylistPosition] = List()
  val publisher = context.system.actorSelection("/user/publisher")

  def receive: Receive = {
    case AddToPlaylist(ytHash) =>
      fetchVideoInfo(ytHash).foreach(playlistPosition => {
        currentQueue = currentQueue :+ playlistPosition
        val payload = Payload("added", Some(Json.toJson(playlistPosition).toString()))
        publisher ! ToPublish(payload)
      })
    case GetPlaylist => sender() ! Playlist(currentQueue)
    case ClearPlaylist => currentQueue = List()
  }

  def fetchVideoInfo(ytHash: String): Option[PlaylistPosition] = {
      restTemplate
        .getForAny[Video](videoUrl, ytHash, youtubeApiKey)
        .flatMap(v => v.items.headOption)
        .map(i => PlaylistPosition(ytHash, i.snippet.title, i.contentDetails.duration))
  }
}
