name := """bard-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
  "org.psnively" % "spring_scala_4-2-0_2.11" % "1.0.0",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.7.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
