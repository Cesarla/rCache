package com.cesarla.persistence

import org.scalatest.{Matchers, WordSpec}

class EncoderSpec extends WordSpec with Matchers  {
  "A Encoder" can {
    "encode" in {
      val dummyEncodeFunction = (_: Int) => new Array[Byte](0)
      Encoder(dummyEncodeFunction).encode(123) should ===( new Array[Byte](0))
    }
  }
}