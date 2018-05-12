package com.cesarla.persistence

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import com.cesarla.data.Fixtures
import com.cesarla.models.{Column, Key, OperationFailed, OperationPerformed}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class KeyRegistrySpec extends WordSpec with Matchers with MockFactory with Fixtures {
  "A Key Registry" can {
    "get an Column" should {
      "return the column if present " in new Scope {
        (mockKvEngine
          .get[String](_: Key[String], _: Instant)(_: Encoder[Key[String]],
                                                   _: Decoder[Column[String]],
                                                   _: ExecutionContext))
          .expects(
            *,
            *,
            *,
            *,
            *
          )
          .returning(Future.successful(Some(columnFixture("test"))))
          .once()

        Await.result(keyRegistry.getColumn(keyFixture, instantFixture), 2.seconds) should ===(
          getOperationPerformedFixture("test"))
      }
      "return an OperationFailed if missing" in new Scope {
        (mockKvEngine
          .get[String](_: Key[String], _: Instant)(_: Encoder[Key[String]],
                                                   _: Decoder[Column[String]],
                                                   _: ExecutionContext))
          .expects(
            *,
            *,
            *,
            *,
            *
          )
          .returning(Future.successful(None))
          .once()

        Await.result(keyRegistry.getColumn(keyFixture, instantFixture), 2.seconds) should ===(
          OperationFailed("get", keyFixture, "key /key not present", StatusCodes.NotFound))
      }
    }

    "set a new  column" in new Scope {
      (mockKvEngine
        .put[String](_: Key[String], _: Column[String])(_: Encoder[Key[String]],
                                                        _: Codec[Column[String]],
                                                        _: ExecutionContext))
        .expects(
          *,
          *,
          *,
          *,
          *
        )
        .returning(Future.successful(()))
        .once()

      Await.result(keyRegistry.setColumn(keyFixture, columnFixture("test")), 2.seconds) should ===(
        OperationPerformed("set"))
    }

    "delete a column" in new Scope {
      (mockKvEngine
        .delete[String](_: Key[String], _: Instant)(_: Encoder[Key[String]],
                                                    _: Codec[Column[String]],
                                                    _: ExecutionContext))
        .expects(
          *,
          *,
          *,
          *,
          *
        )
        .returning(Future.successful(()))
        .once()

      Await.result(keyRegistry.deleteColumn(keyFixture, instantFixture), 2.seconds) should ===(
        OperationPerformed("delete"))
    }
  }

  trait Scope {
    val mockKvEngine: KeyValueEngine = mock[KeyValueEngine]
    val keyRegistry: KeyRegistry = new KeyRegistry(mockKvEngine)
  }
}
