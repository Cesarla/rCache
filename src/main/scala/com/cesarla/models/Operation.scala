package com.cesarla.models

import akka.http.scaladsl.model.StatusCode
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

sealed trait Operation {
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
    extends Operation

object OperationPerformed extends JsonFormatting {
  implicit val jsonFormats: OFormat[OperationPerformed] = Json.format[OperationPerformed]
}

final case class OperationFailed(action: String, key: Key, reason: String, status: StatusCode) extends Operation

object OperationFailed extends JsonFormatting {
  private val statusCodeReads: Reads[StatusCode] = JsPath.read[Int].map(StatusCode.int2StatusCode)
  private val statusCodeWrites: Writes[StatusCode] = Writes[StatusCode](sc => JsNumber(sc.intValue()))
  implicit val statusCodeFormat = Format(statusCodeReads, statusCodeWrites)
  implicit val jsonFormats: OFormat[OperationFailed] = Json.format[OperationFailed]
}
