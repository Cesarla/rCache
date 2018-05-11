package com.cesarla.persistence

import java.time.Instant
import java.util.concurrent.locks.Lock

import com.cesarla.models.{Column, Key}
import com.google.common.util.concurrent.Striped
import org.rocksdb.{Options, RocksDB}

import scala.concurrent.{ExecutionContext, Future}

class RocksDBEngine(rocksDB: RocksDB) extends KeyValueEngine {

  /**
    * RocksDB is not yet exposing the MergeOperator through the Java API,
    * so in the meantime, rCache uses locks at key level to perform a
    * Read-Modify-Write in an atomic fashion.
    */
  private[this] val lazyWeakLock: Striped[Lock] = Striped.lazyWeakLock(256)

  override def get[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                       valueDecoder: ColumnDecoder[A],
                                                       ec: ExecutionContext): Future[Option[Column[A]]] = Future {
    val encodedKey = keyEncoder.encode(key)
    getAndDecode(encodedKey) match {
      case Some(Column(_, _, _, Some(ttl))) if timestamp.getEpochSecond >= ttl.getEpochSecond =>
        rocksDB.delete(encodedKey)
        None
      case e: Option[Column[A]] => e
    }
  }

  override def put[A](key: Key[A], column: Column[A])(implicit keyEncoder: KeyEncoder[A],
                                                      valueFormat: ColumnCodec[A],
                                                      ec: ExecutionContext): Future[Unit] = Future {
    val encodedKey = keyEncoder.encode(key)
    lockByKey(key) {
      if (isColumnNonExistingOrInThePast(encodedKey)(column.timestamp)) {
        val encodedColumn = valueFormat.encode(column)
        rocksDB.put(encodedKey, encodedColumn)
      }
    }
  }

  override def delete[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                          valueReader: ColumnCodec[A],
                                                          ec: ExecutionContext): Future[Unit] = Future {
    val encodedKey = keyEncoder.encode(key)
    lockByKey(key) {
      if (isColumnNonExistingOrInThePast(encodedKey)(timestamp)) {
        rocksDB.delete(encodedKey)
      }
    }
  }

  private[this] def isColumnNonExistingOrInThePast[A](encodedKey: Array[Byte])(timestamp: Instant)(
      implicit valueDecoder: ColumnDecoder[A]): Boolean = {
    val column = getAndDecode(encodedKey)
    column.isEmpty || column.exists(_.timestamp.getEpochSecond < timestamp.getEpochSecond)
  }

  private[this] def getAndDecode[A](encodedKey: Array[Byte])(
      implicit valueDecoder: ColumnDecoder[A]): Option[Column[A]] = {
    Option(rocksDB.get(encodedKey)).flatMap(r => valueDecoder.decode(r))
  }

  private[this] def lockByKey[A, B](key: Key[A])(block: => B): B = {
    val lock: Lock = lazyWeakLock.get(key)
    lock.lock()
    try {
      block
    } finally {
      lock.unlock()
    }
  }
}

object RocksDBEngine {
  def load(path: String): RocksDBEngine = {
    RocksDB.loadLibrary()
    val options = new Options().setCreateIfMissing(true)
    new RocksDBEngine(RocksDB.open(options, path))
  }
}
