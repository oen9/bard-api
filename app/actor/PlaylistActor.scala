package actor

import javax.inject.Inject

import akka.actor.{Actor, ActorSelection, ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.fasterxml.jackson.databind.ObjectMapper
import models.Formats._
import models.MsgTypes._
import models._
import models.youtube.Video
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Await
import scala.concurrent.duration._

class PlaylistActor @Inject() (val reactiveMongoApi: ReactiveMongoApi,
                               implicit val system: ActorSystem,
                               implicit val mat: Materializer,
                               configuration: Configuration,
                               objectMpper: ObjectMapper)
  extends Actor with MongoController with ReactiveMongoComponents {

  val playlistCollection = Await.result(database, Duration.Inf).collection[JSONCollection]("playlist")
  val futurePlaylistInitializer = playlistCollection.find(Json.obj()).cursor[PlaylistPosition]().collect[List](-1, Cursor.ContOnError[List[PlaylistPosition]]())
  var currentQueue: List[PlaylistPosition] = Await.result(futurePlaylistInitializer, Duration.Inf)

  var videoCounter: Option[Cancellable] = None

  val SYNCHRO_MARGIN: FiniteDuration = 2 seconds

  val youtubeApiKey: String = configuration.getString("youtube.api.key").getOrElse("none")
  val videoUrl = s"""https://www.googleapis.com/youtube/v3/videos?id=%s&key=$youtubeApiKey&part=contentDetails,snippet"""

  val publisher: ActorSelection = context.system.actorSelection("/user/publisher")

  def receive: Receive = {
    case msg: Payload if msg.event == PLAY =>
      play(msg)
    case msg: Payload if msg.event == STOP =>
      stop(msg)
    case PlayNext =>
      next()
    case AddToPlaylist(ytHash) =>
      fetchVideoInfo(ytHash)
    case playlistPosition: PlaylistPosition =>
      handleNewPlaylistPosition(playlistPosition)
    case GetPlaylist =>
      sender() ! Playlist(currentQueue)
    case ClearPlaylist =>
      clear()
  }

  def play(msg: Payload): Unit = {
    currentQueue.headOption.foreach(toPlay => {
      publisher ! ToPublish(msg)
      val jd = java.time.Duration.parse(toPlay.duration)
      val duration = Duration(jd.getSeconds, SECONDS).plus(SYNCHRO_MARGIN)
      import system.dispatcher
      videoCounter = Some(system.scheduler.scheduleOnce(duration, self, PlayNext))
    })
  }

  def stop(msg: Payload): Unit = {
    videoCounter.foreach(_.cancel())
    publisher ! ToPublish(msg)
  }

  def next(): Unit = {
    currentQueue = currentQueue.tail
    playlistCollection.remove(BSONDocument(), firstMatchOnly = true)
    publisher ! ToPublish(Payload(NEXT))
    self ! Payload(PLAY)
  }

  def fetchVideoInfo(ytHash: String): Unit = {
    import akka.pattern.pipe

    val url = videoUrl.format(ytHash)

    Http(system).singleRequest(HttpRequest(uri = url)).
      flatMap(httpR => Unmarshal(httpR.entity).to[String]).
      map(s => objectMpper.readValue(s, classOf[Video])).
      map(v => v.items.headOption).
      map(oi => oi.map(i =>PlaylistPosition(ytHash, i.snippet.title, i.contentDetails.duration))).
      filter(_.isDefined).
      map(o => o.get).
      pipeTo(self)
  }

  def handleNewPlaylistPosition(playlistPosition: PlaylistPosition): Unit = {
    currentQueue = currentQueue :+ playlistPosition
    playlistCollection.insert(playlistPosition)

    val payload = Payload(ADDED, Some(Json.toJson(playlistPosition).toString()))
    publisher ! ToPublish(payload)
  }

  def clear(): Unit = {
    currentQueue = List()
    playlistCollection.drop(failIfNotFound = false)
  }
}
