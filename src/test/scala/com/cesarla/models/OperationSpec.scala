package com.cesarla.models

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class OperationSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "OperationPerformed" should {
    "serialize to JSON" in {
      val operationFailed = Json.toJson(getOperationPerformedFixture("value"))
      (operationFailed \ "action").as[String] should === ("get")
      (operationFailed \ "column").as[Column[String]] should === (columnFixture("value"))
    }

    "deserialize from JSON" in {
      val json = Json.parse(
        """
          |{
          |  "action": "get",
          |  "column": {
          |    "key": "key",
          |    "value": "value",
          |    "timestamp": "1970-01-01T00:00:00Z"
          |  }
          |}
        """.stripMargin)
      json.as[OperationPerformed[String]] should === (getOperationPerformedFixture("value"))
    }
  }

  "OperationFailed" should {
    "serialize to JSON" in {
      val operationFailed = Json.toJson(operationFailedFixture[String])
      (operationFailed \ "action").as[String] should === ("get")
      (operationFailed \ "key").as[Key[String]] should === (keyFixture)
      (operationFailed \ "reason").as[String] should === ("key \"key\" not present")
      (operationFailed \ "status").as[Int] should === (StatusCodes.NotFound.intValue)
    }

    "deserialize from JSON" in {
      val json = Json.parse(
        """
          |{
          |  "action": "get",
          |  "key": "key",
          |  "reason": "key \"key\" not present",
          |  "status": 404
          |}
        """.stripMargin)
      json.as[OperationFailed[String]] should === (operationFailedFixture)
    }
  }

}