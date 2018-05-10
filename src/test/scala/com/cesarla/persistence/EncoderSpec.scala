package com.cesarla.persistence

import org.scalatest.{Matchers, WordSpec}

class EncoderSpec extends WordSpec with Matchers  {
  "Encoder" should {
    "encode" in {
      val dummyEncodeFunction = (_: Int) => new Array[Byte](0)
      Encoder(dummyEncodeFunction).encode(123) should ===( new Array[Byte](0))
    }
  }
}