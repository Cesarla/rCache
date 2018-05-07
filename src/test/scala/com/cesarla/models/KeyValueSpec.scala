package com.cesarla.models

import java.time.Instant

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class KeyValueSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "KeyValue" should {
    "serialize" in {
      val keyValue: JsValue = Json.toJson(keyValueWithTtlFixture)

      (keyValue \ "key").as[Key] should === (keyValueWithTtlFixture.key)
      (keyValue \ "value").as[String] should === (keyValueWithTtlFixture.value.value)
      (keyValue \ "ttl").as[Instant] should === (instantFixture)
    }

    "deserialize" in {
      val json = Json.parse(
        """
          |{
          |  "key": "key",
          |  "value": "value",
          |  "ttl": 0
          |}
        """.stripMargin)

        json.as[KeyValue] should === (keyValueWithTtlFixture)
    }
  }

}
