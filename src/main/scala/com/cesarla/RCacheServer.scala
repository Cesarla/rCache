package com.cesarla

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.cesarla.http.KeyValueRoutes

object RCacheServer extends App with KeyValueRoutes {

  implicit val system: ActorSystem = ActorSystem("rCacheActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val keyRegistryActor: ActorRef = system.actorOf(KeyRegistryActor.props, "keyRegistryActor")

  lazy val routes: Route = keyValueRoutes

  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"rCache online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)
}
