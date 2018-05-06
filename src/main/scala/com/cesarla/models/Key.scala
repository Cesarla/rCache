package com.cesarla.models

import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

final case class Key(value: String) extends AnyVal {
  override def toString: String = value
}

object Key extends JsonFormatting {
  val keyReads: Reads[Key] = JsPath.read[String].map(Key.apply)
  val keyWrites: Writes[Key] = Writes[Key](l => JsString(l.value))
  implicit val jsonFormats: Format[Key] = Format(keyReads, keyWrites)
}
