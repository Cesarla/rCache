package com.cesarla.data

import java.time.Instant

import com.cesarla.models.OperationsOps._
import com.cesarla.models._

trait Fixtures {
  lazy val instantFixture = Instant.EPOCH

  lazy val opIdFixture = OpId("op1")

  lazy val byteArrayFixture: Array[Byte] = Array(-84, -19, 0, 5, 116, 0, 5, 118, 97, 108, 117, 101)

  lazy val problemFixture = KeyNotFound("foo")

  def keyFixture[A]: Key[A] = Key[A]("key")
  def columnFixture[A](value: A): Column[A] = Column[A](keyFixture, value, instantFixture)
  def columnFixtureWithTtl[A](value: A): Column[A] = columnFixture(value).copy(ttl = Some(instantFixture))

  def successOperationFixture[A](value: A): Operation[A] = value.asSuccess
  def failureOperationFixture[A]: Operation[A] = problemFixture.asFailure

  def failedResultFixture[A]: FailedResult[A] =
    FailedResult[A]("get", keyFixture[A], s"""key "$keyFixture" not present""")
  def getSuccessResultFixture[A](value: A): SuccessResult[A] = SuccessResult[A]("get", Some(columnFixture(value)))
  def setSuccessResultFixture[A]: SuccessResult[A] = SuccessResult[A]("set", None)
  def deleteSuccessResultFixture[A]: SuccessResult[A] = SuccessResult[A]("delete", None)
}
