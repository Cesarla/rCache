package com.cesarla.models

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

sealed trait Problem {
  val reason: String
  val status: StatusCode
}

final case class StorageError(reason: String) extends Problem {
  val status: StatusCode = StatusCodes.InternalServerError
}

final case class KeyNotFound(reason: String) extends Problem {
  val status: StatusCode = StatusCodes.NotFound
}
