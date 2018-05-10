package com.cesarla.persistence

import org.scalatest.{Matchers, WordSpec}

class ByteArraySerializationSpec extends WordSpec with Matchers  {
  "A ByteArraySerialization" can  {
    val testByteArray: Array[Byte] = Array(-84, -19, 0, 5, 116, 0, 4, 116, 101, 115, 116)
    "serialize" in {
      (new ByteArraySerialization {}).serialize("test") should === (testByteArray)
    }

    "deserialize" should {
      "read the right value from the Byte array" in {
        (new ByteArraySerialization {}).deserialize[String](testByteArray) should === (Some("test"))
      }

      "return None if the deserialized value doesn't match the expected type" in {
        (new ByteArraySerialization {}).deserialize[Int](testByteArray) should === (None)
      }

      "return None when null is passed" in {
        (new ByteArraySerialization {}).deserialize[String](null) should === (None)
      }
    }
  }
}
