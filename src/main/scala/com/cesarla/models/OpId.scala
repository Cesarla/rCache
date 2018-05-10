package com.cesarla.models

import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class OpId(value: String) extends AnyVal {
  override def toString: String = value
}

object OpId extends JsonFormatting {
  implicit val jsonFormats: Format[OpId] =
    Format(JsPath.read[String].map(OpId.apply), Writes[OpId](opId => JsString(opId.value)))
}
