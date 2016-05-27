package actor

import akka.actor.Actor
import models.{AddToPlaylist, ClearPlaylist, GetPlaylist, Playlist}

class PlaylistActor extends Actor {

  var currentQueue: List[String] = List()

  def receive: Receive = {
    case AddToPlaylist(ytHash) => currentQueue = currentQueue :+ ytHash
    case GetPlaylist => sender() ! Playlist(currentQueue)
    case ClearPlaylist => currentQueue = List()
  }
}
