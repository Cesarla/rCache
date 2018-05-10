package com.cesarla.models

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsString, Json}

class KeySpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "Key" should {
    "serialize to JSON" in {
      Json.toJson(keyFixture[Int]) should === (JsString("key"))
      Json.toJson(keyFixture[Boolean]) should === (JsString("key"))
    }

    "deserialize from JSON" in {
      JsString("key").as[Key[Int]] should === (keyFixture[Int])
      JsString("key").as[Key[Boolean]] should === (keyFixture[Boolean])
    }
  }
}
