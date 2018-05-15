package com.cesarla.models

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class ResultSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "ResultSuccess" should {
    "serialize to JSON" in {
      val operationFailed = Json.toJson(getSuccessResultFixture("value"))
      (operationFailed \ "action").as[String] should ===("get")
      (operationFailed \ "column").as[Column[String]] should ===(columnFixture("value"))
    }

    "deserialize from JSON" in {
      val json = Json.parse("""
          |{
          |  "action": "get",
          |  "column": {
          |    "key": "key",
          |    "value": "value",
          |    "timestamp": "1970-01-01T00:00:00Z"
          |  }
          |}
        """.stripMargin)
      json.as[SuccessResult[String]] should ===(getSuccessResultFixture("value"))
    }
  }

  "FailedResult" should {
    "serialize to JSON" in {
      val failedResult = Json.toJson(failedResultFixture[String])
      (failedResult \ "action").as[String] should ===("get")
      (failedResult \ "key").as[Key[String]] should ===(keyFixture)
      (failedResult \ "reason").as[String] should ===("key \"/key\" not present")
    }

    "deserialize from JSON" in {
      val json = Json.parse("""
          |{
          |  "action": "get",
          |  "key": "key",
          |  "reason": "key \"/key\" not present"
          |}
        """.stripMargin)
      json.as[FailedResult[String]] should ===(failedResultFixture)
    }
  }

}
