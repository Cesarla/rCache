package com.cesarla.models

import java.time.Instant

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class ValueSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures{
    "Value" should {
      "serialize" in {
        val keyValue: JsValue = Json.toJson(valueWithTtlFixture)

        (keyValue \ "value").as[String] should === (valueFixture.value)
        (keyValue \ "ttl").as[Instant] should === (instantFixture)
      }

      "deserialize" in {
        val json = Json.parse(
          """
            |{
            |  "value": "foo",
            |  "ttl": 0
            |}
          """.stripMargin)

        json.as[Value] should === (Value("foo", Some(Instant.ofEpochMilli(0l))))
      }
    }
}
