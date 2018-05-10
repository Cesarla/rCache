package com.cesarla.persistence

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.models._

import scala.concurrent.{ExecutionContext, Future}

class KeyRegistry(kvEngine: KeyValueEngine) {
  def getColumn[A](key: Key[A], timestamp: Instant)(implicit ec: ExecutionContext): Future[Operation[A]] = {
    kvEngine
      .get(key, timestamp)
      .map(
        column =>
          if (column.isEmpty) OperationFailed("get", key, s"key /$key not present", StatusCodes.NotFound)
          else OperationPerformed("get", column))
  }

  def setColumn[A](key: Key[A], column: Column[A])(implicit ec: ExecutionContext): Future[Operation[A]] = {
    kvEngine.put(key, column).map(_ => OperationPerformed("set"))
  }

  def deleteColumn[A](key: Key[A], timestamp: Instant)(implicit ec: ExecutionContext): Future[Operation[A]] = {
    kvEngine.delete(key, timestamp).map(_ => OperationPerformed("delete"))
  }
}
