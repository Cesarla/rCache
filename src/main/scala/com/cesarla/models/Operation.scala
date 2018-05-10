package com.cesarla.models

import akka.http.scaladsl.model.StatusCode
import com.cesarla.models.Column.ColumnFormat
import com.cesarla.models.Key.KeyFormat
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

sealed trait Operation[A] {
  def isSuccessful: Boolean = this match {
    case _: OperationPerformed[A] => true
    case _                        => false
  }

  def isFailed: Boolean = this match {
    case _: OperationFailed[A] => true
    case _                     => false
  }
}

final case class OperationPerformed[A](action: String, column: Option[Column[A]] = None) extends Operation[A]

object OperationPerformed extends JsonFormatting {
  implicit def jsonFormats[A: ColumnFormat]: Format[OperationPerformed[A]] =
    Json.format[OperationPerformed[A]]
}

final case class OperationFailed[A](action: String, key: Key[A], reason: String, status: StatusCode)
    extends Operation[A]

object OperationFailed extends JsonFormatting {
  implicit val statusCodeFormat: Format[StatusCode] =
    Format(JsPath.read[Int].map(StatusCode.int2StatusCode), Writes[StatusCode](sc => JsNumber(sc.intValue())))
  implicit def jsonFormats[A: KeyFormat]: Format[OperationFailed[A]] = Json.format[OperationFailed[A]]
}
