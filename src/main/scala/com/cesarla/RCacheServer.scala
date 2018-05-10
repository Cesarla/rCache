package com.cesarla

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.cesarla.http.KeyValueRoutes
import com.cesarla.persistence.{KeyRegistry, RocksDBEngine}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object RCacheServer extends App with KeyValueRoutes {

  implicit val system: ActorSystem = ActorSystem("rCacheActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override val KeyRegistry: KeyRegistry = {
    val rocksDBRunner = RocksDBEngine.load("/tmp/rcache")
    new KeyRegistry(rocksDBRunner)
  }

  lazy val routes: Route = keyValueRoutes

  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"rCache online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)
}
