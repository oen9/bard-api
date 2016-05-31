package models

case class GetPlaylist()
case class AddToPlaylist(ytHash: String)
case class Playlist(playlist: List[PlaylistPosition])
case class PlaylistPosition(ytHash: String, title: String, duration: String)
case class ClearPlaylist()
case class PlayNext()
