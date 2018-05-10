package com.cesarla.persistence

import org.scalatest.{Matchers, WordSpec}

class CodecSpec extends WordSpec with Matchers  {
  val dummyEncodeFunction = (_: Int) => new Array[Byte](0)
  val dummyDecodeFunction = (_: Array[Byte]) => Some(123)
  "Encoder" should {
    "encode" in {
      val codec = Codec(dummyEncodeFunction, dummyDecodeFunction)
      codec.encode(123) should ===( new Array[Byte](0))
    }

    "decode" in {
      val codec = Codec(dummyEncodeFunction, dummyDecodeFunction)
      codec.decode(new Array[Byte](0)) should ===(Some(123))
    }

    "create a Codec from a Encoder and a Decoder" in {
      implicit val encoder: Encoder[Int] = Encoder(dummyEncodeFunction)
      implicit val decoder: Decoder[Int] = Decoder(dummyDecodeFunction)

      def getImplicit(implicit codec: Codec[Int]): Codec[Int] = codec

      getImplicit.encode(123) should ===( new Array[Byte](0))
      getImplicit.decode(new Array[Byte](0)) should ===(Some(123))
    }
  }
}
