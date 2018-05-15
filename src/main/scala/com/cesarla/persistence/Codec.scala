package com.cesarla.persistence

trait Codec[A] extends Decoder[A] with Encoder[A]

object Codec {
  def apply[A](fEncode: A => Array[Byte], fDecode: Array[Byte] => Option[A]): Codec[A] =
    new Codec[A] {
      override def encode(value: A): Array[Byte] = fEncode(value)

      override def decode(value: Array[Byte]): Option[A] = fDecode(value)
    }

  implicit def buildFrom[A](implicit encoder: Encoder[A], decoder: Decoder[A]): Codec[A] =
    apply(encoder.encode, decoder.decode)
}
