package com.cesarla.persistence

import java.time.Instant

import com.cesarla.data.Fixtures
import com.cesarla.models.OperationsOps._
import com.cesarla.models.{Column, KeyNotFound}
import org.rocksdb.RocksDB
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class RocksDBEngineSpec extends WordSpec with Matchers with MockFactory with Fixtures {
  "A RocksDBEngine" can {
    "get a column" should {
      "get an existing key" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()

        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))
        val Right(result) = rocksDBEngine.get[String](keyFixture, instantFixture).await(2.seconds)
        result should ===(columnFixture("value"))
      }
      "delete expired value" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()

        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixtureWithTtl("value")))

        val Left(result) = rocksDBEngine.get[String](keyFixture, Instant.now()).await(2.seconds)
        result should ===(KeyNotFound("key /key not present"))
      }
      "handle key not found" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()

        val Left(result) = rocksDBEngine.get[String](keyFixture, instantFixture).await(2.seconds)
        result should ===(KeyNotFound("key /key not present"))
      }
    }

    "set a column" should {
      "store the column" when {
        "there is not previous value" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
          (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).once()
          val Right(success) = rocksDBEngine.put[String](keyFixture, columnFixture("value")).await(2.seconds)
          success should ===(())
        }

        "there is a value with smaller timestamp" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
          (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).once()
          implicit val dummyDecodeFunction: Decoder[Column[String]] =
            Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))

          val Right(success) = rocksDBEngine
            .put[String](keyFixture, columnFixture("value").copy(timestamp = Instant.now()))
            .await(2.seconds)
          success should ===(())
        }
      }

      "avoid storing the column there is a value with bigger timestamp" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).never()
        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value").copy(timestamp = Instant.now())))
        val Right(success) = rocksDBEngine.put[String](keyFixture, columnFixture("value")).await(2.seconds)
        success should ===(())
      }
    }

    "delete a column" should {
      "delete the column" when {
        "there is not previous value" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
          (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()
          val Right(success) = rocksDBEngine.delete[String](keyFixture, instantFixture).await(2.seconds)
          success should ===(())
        }

        "there is a value with smaller timestamp" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
          (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()
          implicit val dummyDecodeFunction: Decoder[Column[String]] =
            Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))
          val Right(success) = rocksDBEngine.delete[String](keyFixture, Instant.now()).await(2.seconds)
          success should ===(())
        }
      }

      "avoid deleting the column there is a value with bigger timestamp" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()
        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value").copy(timestamp = Instant.now())))
        val Right(success) = rocksDBEngine.delete[String](keyFixture, instantFixture).await(2.seconds)
        success should ===(())
      }
    }
  }

  trait Scope {
    val mockRocksDB: RocksDB = mock[RocksDB]
    val rocksDBEngine: RocksDBEngine = new RocksDBEngine(mockRocksDB)
  }
}
