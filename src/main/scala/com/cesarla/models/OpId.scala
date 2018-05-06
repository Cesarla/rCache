package com.cesarla.models

import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class OpId(value: String) extends AnyVal {
  override def toString: String = value
}

object OpId extends JsonFormatting {
  val opIdReads: Reads[OpId] = JsPath.read[String].map(OpId.apply)
  val opIdWrites: Writes[OpId] = Writes[OpId](l => JsString(l.value))
  implicit val jsonFormats: Format[OpId] = Format(opIdReads, opIdWrites)
}
