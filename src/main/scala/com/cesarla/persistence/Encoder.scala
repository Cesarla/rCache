package com.cesarla.persistence

trait Encoder[A] {
  def encode(value: A): Array[Byte]
}

object Encoder {
  def apply[A](f: A => Array[Byte]): Encoder[A] = (value: A) => f(value)
}
