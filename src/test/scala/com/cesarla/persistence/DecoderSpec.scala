package com.cesarla.persistence

import org.scalatest.{Matchers, WordSpec}

class DecoderSpec extends WordSpec with Matchers  {
  "A Decoder" can {
    "decode" in {
      val dummyDecodeFunction = (_: Array[Byte]) => Some(123)
      Decoder(dummyDecodeFunction).decode(new Array[Byte](0)) should ===(Some(123))
    }
  }
}
