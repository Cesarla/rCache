package com.cesarla.http

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.cesarla.KeyRegistryActor
import com.cesarla.KeyRegistryActor.SetKeyValue
import com.cesarla.models._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class KeyValueRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with KeyValueRoutes with PlayJsonSupport {

  override val keyRegistryActor: ActorRef =
    system.actorOf(KeyRegistryActor.props, "keyRegistryActor")

  lazy val routes: Route = keyValueRoutes

  "UserRoutes" should {
    "GET a key" should {
      "be able to retrieve a present key-value" in {
        val request = Get("/v1/keys/foo")
        keyRegistryActor ! SetKeyValue(OpId("0"), Key("foo"), Value("bar"))
        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed] should ===(OperationPerformed("get", Some(KeyValue(Key("foo"), Value("bar")))))
        }
      }
      "return 404 when the key-value is not present" in {
        val request = Get("/v1/keys/non-existing")
        request ~> routes ~> check {
          status should ===(StatusCodes.NotFound)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed] should ===(OperationFailed("get", Key("non-existing"), "key /non-existing not present", StatusCodes.NotFound))
        }
      }
    }
    "PUT a key" should {
      "be able to set key-value" in {
        val request = Put("/v1/keys/key?value=value")

        request ~> routes ~> check {
          status should ===(StatusCodes.Created)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed] should ===(OperationPerformed("set", Some(KeyValue(Key("key"), Value("value")))))
        }
      }

      "fail if a required parameter is missing (PUT /v1/keys/foo)" in {
        val request = Put("/v1/keys/key")

        request ~> routes ~> check {
          status should ===(StatusCodes.BadRequest)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed] should ===(OperationFailed("set", Key("key"), "Missing required parameter \"value\"", StatusCodes.BadRequest))
        }
      }
    }
    "DELETE a key" should {
      "be able to remove a present key-values" in {
        keyRegistryActor ! SetKeyValue(OpId("0"), Key("delete-me"), Value("value"))
        val request = Delete(uri = "/v1/keys/delete-me")

        request ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationPerformed] should ===(OperationPerformed("delete", None, Some(KeyValue(Key("delete-me"), Value("value")))))
        }
      }
      "return 404 when the key-value is not present" in {
        val request = Delete("/v1/keys/non-existing")
        request ~> routes ~> check {
          status should ===(StatusCodes.NotFound)
          contentType should ===(ContentTypes.`application/json`)
          responseAs[OperationFailed] should ===(OperationFailed("delete", Key("non-existing"), "key /non-existing not present", StatusCodes.NotFound))
        }
      }
    }
  }
}