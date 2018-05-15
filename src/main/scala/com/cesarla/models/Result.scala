package com.cesarla.models

import com.cesarla.models.Column.ColumnFormat
import com.cesarla.models.Key.KeyFormat
import com.cesarla.utils.JsonFormatting
import play.api.libs.json._

trait Result[A]

final case class SuccessResult[A](action: String, column: Option[Column[A]] = None) extends Result[A]

object SuccessResult extends JsonFormatting {
  implicit def jsonFormats[A: ColumnFormat]: Format[SuccessResult[A]] =
    Json.format[SuccessResult[A]]
}

final case class FailedResult[A](action: String, key: Key[A], reason: String) extends Result[A]

object FailedResult extends JsonFormatting {

  implicit def jsonFormats[A: KeyFormat]: Format[FailedResult[A]] = Json.format[FailedResult[A]]

}
