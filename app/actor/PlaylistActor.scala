package actor

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem, Cancellable}
import models._
import models.youtube.Video
import org.psnively.scala.web.client.RestTemplate
import play.api.Configuration
import models.Writes._
import play.api.libs.json.Json

import scala.concurrent.duration._

class PlaylistActor @Inject() (configuration: Configuration, system: ActorSystem) extends Actor {

  val SYNCHRO_MARGIN = 2 seconds

  val videoUrl = """https://www.googleapis.com/youtube/v3/videos?id={ytHash}&key={apiKey}&part=contentDetails,snippet"""
  val youtubeApiKey = configuration.getString("youtube.api.key").getOrElse("none")
  val restTemplate = new RestTemplate()

  var currentQueue: List[PlaylistPosition] = List()
  var videoCounter: Option[Cancellable] = None
  val publisher = context.system.actorSelection("/user/publisher")

  def receive: Receive = {
    case msg: Payload if msg.event == "play" =>
      currentQueue.headOption.foreach(toPlay => {
        publisher ! ToPublish(msg)
        val jd = java.time.Duration.parse(toPlay.duration)
        val duration = Duration(jd.getSeconds, SECONDS).plus(SYNCHRO_MARGIN)
        import system.dispatcher
        videoCounter = Some(system.scheduler.scheduleOnce(duration, self, PlayNext))
      })
    case msg: Payload if msg.event == "stop" => {
      videoCounter.foreach(_.cancel())
      publisher ! ToPublish(msg)
    }
    case PlayNext => {
      currentQueue = currentQueue.tail
      publisher ! ToPublish(Payload("next"))
      self ! Payload("play")
    }

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
