package modules

import actor.{ActorInit, PlaylistActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class EagerInit extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[ActorInit]).asEagerSingleton()
    bindActor[PlaylistActor]("playlist")
  }
}
