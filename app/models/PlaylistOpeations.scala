package models

case class GetPlaylist()
case class AddToPlaylist(ytHash: String)
case class Playlist(playlist: List[String])
case class ClearPlaylist()
