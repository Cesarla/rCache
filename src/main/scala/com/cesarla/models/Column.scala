package com.cesarla.models

import java.time.Instant

import com.cesarla.persistence.{ByteArraySerialization, Decoder, Encoder}
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class Column[A](key: Key[A], value: A, timestamp: Instant, ttl: Option[Instant] = None)

object Column extends JsonFormatting with ByteArraySerialization {

  type ColumnFormat[A] = Format[Column[A]]

  implicit def columnEncoder[A]: Encoder[Column[A]] = Encoder[Column[A]] { column: Column[A] =>
    serialize(column)
  }

  implicit def columnDecoder[A]: Decoder[Column[A]] = Decoder[Column[A]] { bytes =>
    deserialize[Column[A]](bytes)
  }

  implicit def columnFormat[A: Format]: Format[Column[A]] = Json.format[Column[A]]
}
