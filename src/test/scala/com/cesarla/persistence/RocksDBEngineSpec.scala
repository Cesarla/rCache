package com.cesarla.persistence

import java.time.Instant

import com.cesarla.data.Fixtures
import com.cesarla.models.Column
import org.rocksdb.RocksDB
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RocksDBEngineSpec extends WordSpec with Matchers with MockFactory with Fixtures {
  "A RocksDBEngine" can {
    "get a column" should {
      "get an existing key" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()

        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))
        val result: Option[Column[String]] =
          Await.result(rocksDBEngine.get[String](keyFixture, instantFixture), 2.seconds)

        result should ===(Some(columnFixture("value")))
      }
      "delete expired value" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()

        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixtureWithTtl("value")))
        val result: Option[Column[String]] =
          Await.result(rocksDBEngine.get[String](keyFixture, Instant.now()), 2.seconds)

        result should ===(None)
      }
      "handle key not found" in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()

        val result: Option[Column[String]] =
          Await.result(rocksDBEngine.get[String](keyFixture, instantFixture), 2.seconds)

        result should ===(None)
      }
    }

    "set a column" should {
      "store the column" when {
        "there is not previous value" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
          (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).once()
          Await.result(rocksDBEngine.put[String](keyFixture, columnFixture("value")), 2.seconds) should ===(())
        }

        "there is a value with smaller timestamp" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
          (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).once()
          implicit val dummyDecodeFunction: Decoder[Column[String]] =
            Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))
          Await.result(rocksDBEngine.put[String](keyFixture, columnFixture("value").copy(timestamp = Instant.now())),
                       2.seconds) should ===(())
        }
      }

      "avoid storing the column there is a value with bigger timestamp " in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.put(_: Array[Byte], _: Array[Byte])).expects(*, *).returning(()).never()
        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value").copy(timestamp = Instant.now())))
        Await.result(rocksDBEngine.put[String](keyFixture, columnFixture("value")), 2.seconds) should ===(())
      }
    }

    "delete a column" should {
      "delete the column" when {
        "there is not previous value" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(null).once()
          (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()
          Await.result(rocksDBEngine.delete[String](keyFixture, instantFixture), 2.seconds) should ===(())
        }

        "there is a value with smaller timestamp" in new Scope {
          (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
          (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).once()
          implicit val dummyDecodeFunction: Decoder[Column[String]] =
            Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value")))
          Await.result(rocksDBEngine.delete[String](keyFixture, Instant.now()), 2.seconds) should ===(())
        }
      }

      "avoid deleting the column there is a value with bigger timestamp " in new Scope {
        (mockRocksDB.get(_: Array[Byte])).expects(*).returning(byteArrayFixture).once()
        (mockRocksDB.delete(_: Array[Byte])).expects(*).returning(()).never()
        implicit val dummyDecodeFunction: Decoder[Column[String]] =
          Decoder[Column[String]]((_: Array[Byte]) => Some(columnFixture("value").copy(timestamp = Instant.now())))
        Await.result(rocksDBEngine.delete[String](keyFixture, instantFixture), 2.seconds) should ===(())
      }
    }
  }

  trait Scope {
    val mockRocksDB: RocksDB = mock[RocksDB]
    val rocksDBEngine: RocksDBEngine = new RocksDBEngine(mockRocksDB)
  }
}
