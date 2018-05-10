package com.cesarla.persistence

trait Decoder[A] {
  def decode(value: Array[Byte]): Option[A]
}

object Decoder {
  def apply[A](f: Array[Byte] => Option[A]): Decoder[A] = (value: Array[Byte]) => f(value)
}
