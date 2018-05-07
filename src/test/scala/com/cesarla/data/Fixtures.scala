package com.cesarla.data

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.models._

trait Fixtures {
  lazy val instantFixture = Instant.EPOCH

  lazy val opIdFixture = OpId("op1")

  lazy val keyFixture = Key("key")
  lazy val valueFixture = Value("value")
  lazy val valueWithTtlFixture = valueFixture.copy(ttl = Some(instantFixture))
  lazy val keyValueFixture = KeyValue(keyFixture, valueFixture)
  lazy val keyValueWithTtlFixture = KeyValue(keyFixture, valueWithTtlFixture)

  lazy val operationFailedFixture = OperationFailed("get", keyFixture, s"""key "$keyFixture" not present""", StatusCodes.NotFound)
  lazy val operationPerformedFixture = OperationPerformed("get", Some(keyValueFixture))
}
