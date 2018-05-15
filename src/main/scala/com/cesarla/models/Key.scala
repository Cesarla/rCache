package com.cesarla.models

import com.cesarla.persistence.{ByteArraySerialization, Encoder}
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class Key[A](value: String) extends AnyVal {
  override def toString: String = s"/$value"
}

object Key extends JsonFormatting with ByteArraySerialization {
  type KeyFormat[A] = Format[Key[A]]

  implicit def keyEncoder[A]: Encoder[Key[A]] = Encoder[Key[A]](this.serialize)

  implicit def jsonFormats[A: Format]: Format[Key[A]] =
    Format(JsPath.read[String].map(Key.apply), Writes[Key[A]](l => JsString(l.value)))
}
