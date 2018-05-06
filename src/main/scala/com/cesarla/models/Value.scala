package com.cesarla.models

import java.time.Instant

import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class Value(value: String, ttl: Option[Instant] = None)

object Value extends JsonFormatting {
  implicit val jsonFormats: Format[Value] = Json.format[Value]
}
