package com.cesarla.models

import akka.http.scaladsl.model.StatusCode
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

sealed abstract class Operation(action: String) {
  def isSuccessful: Boolean = this match {
    case _: OperationPerformed => true
    case _                     => false
  }

  def isFailed: Boolean = this match {
    case _: OperationFailed => true
    case _                  => false
  }
}

final case class OperationPerformed(action: String,
                                    keyValue: Option[KeyValue] = None,
                                    oldKeyValue: Option[KeyValue] = None)
    extends Operation(action)

object OperationPerformed extends JsonFormatting {
  implicit val jsonFormats: OFormat[OperationPerformed] = Json.format[OperationPerformed]
}

final case class OperationFailed(action: String, key: Key, reason: String, status: StatusCode) extends Operation(action)

object OperationFailed extends JsonFormatting {
  private implicit val opIdReads: Reads[StatusCode] = JsPath.read[Int].map(StatusCode.int2StatusCode)
  private implicit val opIdWrites: Writes[StatusCode] = Writes[StatusCode](sc => JsNumber(sc.intValue()))
  implicit val jsonFormats: OFormat[OperationFailed] = Json.format[OperationFailed]
}
