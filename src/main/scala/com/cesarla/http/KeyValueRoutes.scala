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
import scala.concurrent.duration._

trait KeyValueRoutes extends PlayJsonSupport {

  implicit def system: ActorSystem

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  lazy val log = Logging(system, classOf[KeyValueRoutes])

  val counter = new AtomicLong()

  val KeyRegistry: KeyRegistry

  lazy val keyValueRoutes: Route =
    pathPrefix("v1" / "keys") {
      val id = OpId(counter.getAndIncrement().toString)

      path(Segment) { key =>
        concat(
          get {
            val operation: Operation[Column[String]] = KeyRegistry.getColumn(Key(key), Instant.now())
            onSuccess(operation.value) {
              case Right(column) => complete((StatusCodes.OK, SuccessResult("get", Some(column))))
              case Left(problem: Problem) =>
                val result: FailedResult[String] = FailedResult("get", Key(key), problem.reason)
                log.info("{} - Key {} failed to be fetched: {}", id, key, result)
                complete((problem.status, result))
            }
          },
          put {
            parameters(('value.as[String].?, 'ttl.as[Long].?)) {
              case (Some(value), ttl) =>
                val operation: Operation[Unit] =
                  KeyRegistry.setColumn(Key[String](key),
                                        Column(Key[String](key), value, Instant.now(), ttl.map(Instant.ofEpochSecond)))
                onSuccess(operation.value) {
                  case Right(_) => complete((StatusCodes.Created, SuccessResult[String]("set")))
                  case Left(problem: Problem) =>
                    val result = FailedResult("set", Key[String](key), problem.reason)
                    log.info("{} - Key {} failed to be set: {}", id, key, result)
                    complete((problem.status, result))
                }
              case (None, _) =>
                val result: FailedResult[String] =
                  FailedResult("set", Key(key), "Missing required parameter \"value\"")
                complete((StatusCodes.BadRequest, result))
            }
          },
          delete {
            val operation: Operation[Unit] = KeyRegistry.deleteColumn(Key(key), Instant.now())
            onSuccess(operation.value) {
              case Right(_) =>
                complete((StatusCodes.OK, SuccessResult[String]("delete")))
              case Left(problem: Problem) =>
                val result = FailedResult("delete", Key[String](key), problem.reason)
                log.info("{} - Key {} failed to be deleted: {}", id, key, result)
                complete((problem.status, result))
            }
          }
        )
      }
    }
}
