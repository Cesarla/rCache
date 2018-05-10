package com.cesarla.http

import java.time.Instant

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.cesarla.data.Fixtures
import com.cesarla.models._
import com.cesarla.persistence.KeyRegistry
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{ExecutionContext, Future}

class KeyValueRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with KeyValueRoutes with PlayJsonSupport  with Fixtures with MockFactory {

  lazy val routes: Route = keyValueRoutes

  override val KeyRegistry: KeyRegistry = mock[KeyRegistry]

  "KeyValueRoutes" when {
    "GET a key" should {
      "be able to retrieve a present key-value" in {
        val operation = getOperationPerformedFixture[String]("foo")
        (KeyRegistry.getColumn[String](_:Key[String],_:Instant)(_:ExecutionContext))
          .expects(*, *, *).returning(Future.successful(operation))

        val request = Get("/v1/keys/foo")
        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed[String]] should ===(operation)
        }
      }
      "return 404 when the key-value is not present" in {
        val operationFailed: Operation[String] = OperationFailed("get", Key[String]("non-existing"), "key /non-existing not present", StatusCodes.NotFound)
        (KeyRegistry.getColumn[String](_:Key[String],_:Instant)(_:ExecutionContext))
          .expects(*, *, *).returning(Future.successful(operationFailed))

        val request = Get("/v1/keys/non-existing")
        request ~> routes ~> check {
          status should ===(StatusCodes.NotFound)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed[String]] should ===(operationFailed)
        }
      }
    }
    "PUT a key" should {
      "be able to set key-value" in {
        val operation: Operation[String] = setOperationPerformedFixture[String]("value")
        (KeyRegistry.setColumn[String](_:Key[String],_:Column[String])(_:ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(operation))

        val request = Put("/v1/keys/key?value=value")
        request ~> routes ~> check {
          status should ===(StatusCodes.Created)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed[String]] should ===(operation)
        }
      }

      "fail if a required parameter is missing (PUT /v1/keys/foo)" in {
        val request = Put("/v1/keys/key")
        request ~> routes ~> check {
          status should ===(StatusCodes.BadRequest)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed[String]] should ===(OperationFailed("set", Key("key"), "Missing required parameter \"value\"", StatusCodes.BadRequest))
        }
      }
    }
    "DELETE a key" should {
      "be able to remove a present key-values" in {
        val operation: Operation[String] = deleteOperationPerformedFixture[String]
        (KeyRegistry.deleteColumn[String](_:Key[String],_:Instant)(_:ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(operation))

        val request = Delete(uri = "/v1/keys/delete-me")
        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed[String]] should ===(operation)
        }
      }
      "return 404 when the key-value is not present" in {
        val operation: Operation[String] = OperationFailed("delete", Key("non-existing"), "key /non-existing not present", StatusCodes.NotFound)
        (KeyRegistry.deleteColumn[String](_:Key[String],_:Instant)(_:ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(operation))


        val request = Delete("/v1/keys/non-existing")
        request ~> routes ~> check {
          status should ===(StatusCodes.NotFound)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed[String]] should ===(operation)
        }
      }
    }
  }
}