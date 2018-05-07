package com.cesarla.models

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsString, Json}

class KeySpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "Key" should {
    "serialize" in {
      Json.toJson(keyFixture) should === (JsString("key"))
    }

    "deserialize" in {
      JsString("key").as[Key] should === (keyFixture)
    }
  }
}
