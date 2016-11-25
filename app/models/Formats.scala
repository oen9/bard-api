package models

import play.api.libs.json.Json

object Formats {
  implicit val playlistPositionWrites = Json.writes[PlaylistPosition]
  implicit val playlistWrites = Json.writes[Playlist]

  implicit val playlistPositionFormat = Json.format[PlaylistPosition]
}
