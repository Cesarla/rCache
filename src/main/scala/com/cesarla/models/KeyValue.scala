package com.cesarla.models

import java.time.Instant

import com.cesarla.utils.JsonFormatting
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }

final case class KeyValue(key: Key, value: Value)

object KeyValue extends JsonFormatting {
  def unapply(kv: KeyValue) = Some((kv.key, kv.value.value, kv.value.ttl))

  implicit val keyValueWrites: Writes[KeyValue] = (
    (JsPath \ "key").write[Key] and
    (JsPath \ "value").write[String] and
    (JsPath \ "ttl").writeNullable[Instant])(unlift(KeyValue.unapply))

  implicit val keyValueReads: Reads[KeyValue] = (
    (JsPath \ "key").read[Key] and
    JsPath.read[Value])(KeyValue.apply _)
}

