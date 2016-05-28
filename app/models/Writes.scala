package models

import play.api.libs.json.Json

object Writes {
  implicit val playlistPositionWrites = Json.writes[PlaylistPosition]
  implicit val playlistWrites = Json.writes[Playlist]
}
