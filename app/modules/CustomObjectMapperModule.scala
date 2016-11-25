package modules

import javax.inject.{Inject, Provider, Singleton}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment}
import play.libs.Json

import scala.concurrent.Future

class CustomObjectMapperModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[ObjectMapper].toProvider[ObjectMapperProvider].eagerly()
  )
}

@Singleton
class ObjectMapperProvider @Inject() (lifecycle: ApplicationLifecycle) extends Provider[ObjectMapper] {

  lazy val get : ObjectMapper = {
    val objectMapper = Json.newDefaultMapper()
    objectMapper.registerModule(DefaultScalaModule)

    Json.setObjectMapper(objectMapper)
    lifecycle.addStopHook { () =>
      Future.successful(Json.setObjectMapper(null))
    }
    objectMapper
  }
}