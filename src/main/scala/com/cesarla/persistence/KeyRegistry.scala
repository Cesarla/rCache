package com.cesarla.persistence

import java.time.Instant

import com.cesarla.models._

import scala.concurrent.ExecutionContext

class KeyRegistry(kvEngine: KeyValueEngine) {
  def getColumn[A](key: Key[A], timestamp: Instant)(implicit ec: ExecutionContext): Operation[Column[A]] = {
    kvEngine.get(key, timestamp)
  }

  def setColumn[A](key: Key[A], column: Column[A])(implicit ec: ExecutionContext): Operation[Unit] = {
    kvEngine.put(key, column)
  }

  def deleteColumn[A](key: Key[A], timestamp: Instant)(implicit ec: ExecutionContext): Operation[Unit] = {
    kvEngine.delete(key, timestamp)
  }
}
