package com.cesarla

import akka.actor.ActorSystem
import com.cesarla.ring.MembershipListener
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.cesarla.http.KeyValueRoutes
import com.cesarla.persistence.{KeyRegistry, RocksDBEngine}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object RCacheServer extends App {

  val nodes: Seq[Node] = Seq(8080, 8082, 8084).map(port => new Node(port))

  nodes.map(node => Await.result(node.system.whenTerminated, Duration.Inf))
}

class Node(httpPort: Int) extends KeyValueRoutes {
  val config = ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.port=${httpPort + 1}
        akka.remote.artery.canonical.port=${httpPort + 1}
        """).withFallback(ConfigFactory.load())

  implicit val system: ActorSystem = ActorSystem(s"rCacheSystem", config)

  override val keyRegistry: KeyRegistry = {
    val rocksDBRunner = RocksDBEngine.load(s"/tmp/rcache-$httpPort")
    new KeyRegistry(rocksDBRunner)
  }

  system.actorOf(MembershipListener.props(httpPort.toString), name = "memberhipListener")

  lazy val routes: Route = keyValueRoutes

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  Http().bindAndHandle(routes, "localhost", httpPort)
  println(s"rCache online at http://localhost:$httpPort/")
}
