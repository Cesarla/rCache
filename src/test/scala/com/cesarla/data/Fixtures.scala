package com.cesarla.data

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.models._

trait Fixtures {
  lazy val instantFixture = Instant.EPOCH

  lazy val opIdFixture = OpId("op1")

  lazy val byteArrayFixture: Array[Byte] = Array(-84, -19, 0, 5, 116, 0, 5, 118, 97, 108, 117, 101)

  def keyFixture[A]: Key[A] = Key[A]("key")
  def columnFixture[A](value:A): Column[A] = Column[A](keyFixture, value, instantFixture)
  def columnFixtureWithTtl[A](value:A): Column[A] = columnFixture(value).copy(ttl=Some(instantFixture))

  def operationFailedFixture[A]: OperationFailed[A] = OperationFailed[A]("get", keyFixture[A], s"""key "$keyFixture" not present""", StatusCodes.NotFound)
  def getOperationPerformedFixture[A](value:A): OperationPerformed[A] = OperationPerformed[A]("get", Some(columnFixture(value)))
  def setOperationPerformedFixture[A](value:A): OperationPerformed[A] = OperationPerformed[A]("set", Some(columnFixture(value)))
  def deleteOperationPerformedFixture[A]: OperationPerformed[A] = OperationPerformed[A]("delete", None)
}
