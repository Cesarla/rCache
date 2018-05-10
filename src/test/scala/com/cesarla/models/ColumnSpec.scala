package com.cesarla.models

import java.time.Instant

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class ColumnSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "KeyValue" should {
    "serialize" in {
      val column: Column[String] = columnFixtureWithTtl("test")
      val keyValue: JsValue = Json.toJson(column)

      (keyValue \ "key").as[Key[String]] should === (column.key)
      (keyValue \ "value").as[String] should === (column.value)
      (keyValue \ "timestamp").as[Instant] should === (column.timestamp)
      (keyValue \ "ttl").as[Instant] should === (instantFixture)
    }

    "deserialize" in {
      val json = Json.parse(
        """
          |{
          |  "key": "key",
          |  "value": "value",
          |  "timestamp": 0,
          |  "ttl": 0
          |}
        """.stripMargin)

        json.as[Column[String]] should === (columnFixtureWithTtl("value"))
    }
  }

}
