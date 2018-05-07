package com.cesarla.models

import com.cesarla.data.Fixtures
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsString, Json}

class OpIdSpec extends WordSpec with Matchers with PlayJsonSupport with Fixtures {
  "OpId" should {
    "serialize" in {
      Json.toJson(opIdFixture) should === (JsString("op1"))
    }

    "deserialize" in {
      JsString("op1").as[OpId] should === (opIdFixture)
    }
  }
}