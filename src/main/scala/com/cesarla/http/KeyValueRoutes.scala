package com.cesarla.http

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.cesarla.KeyRegistryActor.{SetKeyValue, _}
import com.cesarla.models._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

trait KeyValueRoutes extends PlayJsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[KeyValueRoutes])

  def keyRegistryActor: ActorRef

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  val counter = new AtomicLong()

  lazy val keyValueRoutes: Route =
    pathPrefix("v1" / "keys") {
      val id = OpId(counter.getAndIncrement().toString)

      path(Segment) { key =>
        concat(
          get {
            val operation: Future[Operation] =
              (keyRegistryActor ? GetKeyValue(id, Key(key))).mapTo[Operation]
            onSuccess(operation) {
              case op: OperationPerformed => complete((StatusCodes.OK, op))
              case op: OperationFailed =>
                log.info("{} - Key {} failed to be fetched: {}", id, key, op)
                complete((op.status, op))
            }
          },
          put {
            parameters('value.as[String].?, 'ttl.as[Long].?) {
              case (Some(value), ttl) =>
                val operation: Future[Operation] =
                  (keyRegistryActor ? SetKeyValue(id, Key(key), Value(value, ttl.map(Instant.ofEpochSecond))))
                    .mapTo[Operation]
                onSuccess(operation) {
                  case op: OperationPerformed => complete((StatusCodes.Created, op))
                  case op: OperationFailed =>
                    log.info("{} - Key {} failed to be fetched: {}", id, key, op)
                    complete((op.status, op))
                }
              case (None, _) =>
                val op =
                  OperationFailed("set", Key(key), "Missing required parameter \"value\"", StatusCodes.BadRequest)
                complete((op.status, op))
            }
          },
          delete {
            val operation: Future[Operation] =
              (keyRegistryActor ? DeleteKeyValue(id, Key(key))).mapTo[Operation]
            onSuccess(operation) {
              case op: OperationPerformed =>
                log.info("{} - Key {} deleted", id, key)
                complete((StatusCodes.OK, op))
              case op: OperationFailed =>
                log.info("{} - Key {} failed to be deleted: {}", id, key, op)
                complete((op.status, op))
            }
          }
        )
      }
    }
}
