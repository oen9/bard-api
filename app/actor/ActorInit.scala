package actor

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}

@Singleton
class ActorInit @Inject()(system: ActorSystem) {
  val publisher = system.actorOf(Props[Publisher], "publisher")
}
