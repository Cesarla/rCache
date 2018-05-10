package com.cesarla.persistence

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.reflect.ClassTag

trait ByteArraySerialization {
  def serialize[A](value: A): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val objectOutputStream = new ObjectOutputStream(stream)
    try {
      objectOutputStream.writeObject(value)
    } finally {
      objectOutputStream.close()
    }
    stream.toByteArray
  }

  def deserialize[A](bytes: Array[Byte])(implicit tag: ClassTag[A]): Option[A] = {
    Option(bytes)
      .map { bytes =>
        val objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))
        try {
          objectInputStream.readObject
        } finally {
          objectInputStream.close()
        }
      }
      .collect { case t: A => t }
  }
}
