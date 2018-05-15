package com.cesarla.persistence

import java.time.Instant

import com.cesarla.data.Fixtures
import com.cesarla.models.OperationsOps._
import com.cesarla.models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
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
          .returning(columnFixture("test").asSuccess)
          .once()

        val Right(success) = keyRegistry.getColumn(keyFixture[String], instantFixture).await(2.seconds)
        success should ===(columnFixture[String]("test"))
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
          .returning(KeyNotFound("key /key not present").asFailure)
          .once()

        val Left(problem) = keyRegistry.getColumn(keyFixture[String], instantFixture).await(2.seconds)
        problem should ===(KeyNotFound("key /key not present"))
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
        .returning(().asSuccess)
        .once()

      val Right(success) = keyRegistry.setColumn(keyFixture, columnFixture("test")).await(2.seconds)
      success should ===(())
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
        .returning(().asSuccess)
        .once()

      val Right(success) = keyRegistry.deleteColumn(keyFixture, instantFixture).await(2.seconds)
      success should ===(())
    }
  }

  trait Scope {
    val mockKvEngine: KeyValueEngine = mock[KeyValueEngine]
    val keyRegistry: KeyRegistry = new KeyRegistry(mockKvEngine)
  }
}
