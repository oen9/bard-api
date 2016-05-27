package modules

import actor.ActorInit
import com.google.inject.AbstractModule

class EagerInit extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ActorInit]).asEagerSingleton()
  }
}
