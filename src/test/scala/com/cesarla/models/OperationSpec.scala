package com.cesarla.models

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class OperationSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "OperationPerformed" should {
    "serialize" in {
      val operationFailed = Json.toJson(operationPerformedFixture)
      (operationFailed \ "action").as[String] should === ("get")
      (operationFailed \ "key_value").as[KeyValue] should === (keyValueFixture)
    }

    "deserialize" in {
      val json = Json.parse(
        """
          |{
          |  "action": "get",
          |  "key_value": {
          |    "key": "key",
          |    "value": "value"
          |  }
          |}
        """.stripMargin)
      json.as[OperationPerformed] should === (operationPerformedFixture)
    }
  }

  "OperationFailed" should {
    "serialize" in {
      val operationFailed = Json.toJson(operationFailedFixture)
      (operationFailed \ "action").as[String] should === ("get")
      (operationFailed \ "key").as[Key] should === (keyFixture)
      (operationFailed \ "reason").as[String] should === ("key \"key\" not present")
      (operationFailed \ "status").as[Int] should === (StatusCodes.NotFound.intValue)
    }

    "deserialize" in {
      val json = Json.parse(
        """
          |{
          |  "action": "get",
          |  "key": "key",
          |  "reason": "key \"key\" not present",
          |  "status": 404
          |}
        """.stripMargin)
      json.as[OperationFailed] should === (operationFailedFixture)
    }
  }

}