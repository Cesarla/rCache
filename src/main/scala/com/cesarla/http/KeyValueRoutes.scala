package com.cesarla.http

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.cesarla.models._
import com.cesarla.persistence.KeyRegistry
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait KeyValueRoutes extends PlayJsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[KeyValueRoutes])

  def KeyRegistry: KeyRegistry

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  val counter = new AtomicLong()

  lazy val keyValueRoutes: Route =
    pathPrefix("v1" / "keys") {
      val id = OpId(counter.getAndIncrement().toString)

      path(Segment) { key =>
        concat(
          get {
            val operation: Future[Operation[String]] = KeyRegistry.getColumn(Key(key), Instant.now())
            onSuccess(operation) {
              case op: OperationPerformed[String] => complete((StatusCodes.OK, op))
              case op: OperationFailed[String] =>
                log.info("{} - Key {} failed to be fetched: {}", id, key, op)
                complete((op.status, op))
            }
          },
          put {
            parameters(('value.as[String].?, 'ttl.as[Long].?)) {
              case (Some(value), ttl) =>
                val operation: Future[Operation[String]] =
                  KeyRegistry.setColumn(Key(key),
                                        Column(Key(key), value, Instant.now(), ttl.map(Instant.ofEpochSecond)))
                onSuccess(operation) {
                  case op: OperationPerformed[String] => complete((StatusCodes.Created, op))
                  case op: OperationFailed[String] =>
                    log.info("{} - Key {} failed to be set: {}", id, key, op)
                    complete((op.status, op))
                }
              case (None, _) =>
                val op: OperationFailed[String] =
                  OperationFailed("set", Key(key), "Missing required parameter \"value\"", StatusCodes.BadRequest)
                complete((op.status, op))
            }
          },
          delete {
            val operation: Future[Operation[String]] = KeyRegistry.deleteColumn(Key(key), Instant.now())
            onSuccess(operation) {
              case op: OperationPerformed[String] =>
                log.info("{} - Key {} deleted", id, key)
                complete((StatusCodes.OK, op))
              case op: OperationFailed[String] =>
                log.info("{} - Key {} failed to be deleted: {}", id, key, op)
                complete((op.status, op))
            }
          }
        )
      }
    }
}
