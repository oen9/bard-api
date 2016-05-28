package models.youtube

case class Video(items: List[Item])
case class Item(snippet: Snippet, contentDetails: ContentDetails)
case class Snippet(title: String)
case class ContentDetails(duration: String)
