package com.cesarla.http

import java.time.Instant

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.cesarla.data.Fixtures
import com.cesarla.models.OperationsOps._
import com.cesarla.models._
import com.cesarla.persistence.KeyRegistry
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext

class KeyValueRoutesSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with KeyValueRoutes
    with PlayJsonSupport
    with Fixtures
    with MockFactory {

  lazy val routes: Route = keyValueRoutes

  override val KeyRegistry: KeyRegistry = mock[KeyRegistry]

  "KeyValueRoutes" when {
    "GET a key" should {
      "be able to retrieve a present key-value" in {
        val operation: Operation[Column[String]] = successOperationFixture(columnFixture("foo"))
        val result: SuccessResult[String] = getSuccessResultFixture[String]("foo")
        (KeyRegistry
          .getColumn[String](_: Key[String], _: Instant)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Get("/v1/keys/foo")
        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[SuccessResult[String]] should ===(result)
        }
      }
      "return 404 when the key-value is not present" in {
        val operation: Operation[Column[String]] = KeyNotFound("key /non-existing not present").asFailure
        val result: FailedResult[String] =
          FailedResult[String]("get", Key[String]("non-existing"), "key /non-existing not present")
        (KeyRegistry
          .getColumn[String](_: Key[String], _: Instant)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Get("/v1/keys/non-existing")
        request ~> routes ~> check {
          status should ===(StatusCodes.NotFound)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[FailedResult[String]] should ===(result)
        }
      }
      "return 500 for an unexpected errors" in {
        val operation: Operation[Column[String]] = StorageError("Some low level error").asFailure
        val result: FailedResult[String] =
          FailedResult[String]("get", Key[String]("foo"), "Some low level error")
        (KeyRegistry
          .getColumn[String](_: Key[String], _: Instant)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Get("/v1/keys/foo")
        request ~> routes ~> check {
          status should ===(StatusCodes.InternalServerError)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[FailedResult[String]] should ===(result)
        }
      }
    }
    "PUT a key" should {
      "be able to set key-value" in {
        val operation: Operation[Unit] = ().asSuccess
        val result: SuccessResult[String] = setSuccessResultFixture[String]
        (KeyRegistry
          .setColumn[String](_: Key[String], _: Column[String])(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Put("/v1/keys/key?value=value")
        request ~> routes ~> check {
          status should ===(StatusCodes.Created)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[SuccessResult[String]] should ===(result)
        }
      }

      "return 400 if a required parameter is missing" in {
        val request = Put("/v1/keys/key")
        request ~> routes ~> check {
          status should ===(StatusCodes.BadRequest)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[FailedResult[String]] should ===(
            FailedResult("set", Key("key"), "Missing required parameter \"value\""))
        }
      }

      "return 500 for an unexpected errors" in {
        val operation: Operation[Unit] = StorageError("Some low level error").asFailure
        val result: FailedResult[String] =
          FailedResult("set", Key("key"), "Some low level error")
        (KeyRegistry
          .setColumn[String](_: Key[String], _: Column[String])(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Put("/v1/keys/key?value=value")
        request ~> routes ~> check {
          status should ===(StatusCodes.InternalServerError)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[FailedResult[String]] should ===(result)
        }
      }
    }
    "DELETE a key" should {
      "be able to remove a present key-values" in {
        val operation: Operation[Unit] = ().asSuccess
        val result: SuccessResult[String] = deleteSuccessResultFixture[String]
        (KeyRegistry
          .deleteColumn[String](_: Key[String], _: Instant)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Delete(uri = "/v1/keys/delete-me")
        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[SuccessResult[String]] should ===(result)
        }
      }
      "return 500 for an unexpected errors" in {
        val operation: Operation[Unit] = StorageError("Some low level error").asFailure
        val result: FailedResult[String] =
          FailedResult("delete", Key("foo"), "Some low level error")
        (KeyRegistry
          .deleteColumn[String](_: Key[String], _: Instant)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(operation)

        val request = Delete("/v1/keys/foo")
        request ~> routes ~> check {
          status should ===(StatusCodes.InternalServerError)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[FailedResult[String]] should ===(result)
        }
      }
    }
  }
}
