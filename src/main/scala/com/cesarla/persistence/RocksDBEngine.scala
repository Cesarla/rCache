package com.cesarla.persistence

import java.time.Instant
import java.util.concurrent.locks.Lock

import com.cesarla.models.Operation._
import com.cesarla.models._
import com.google.common.util.concurrent.Striped
import org.rocksdb.{Options, RocksDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RocksDBEngine(rocksDB: RocksDB) extends KeyValueEngine {

  /**
    * RocksDB is not yet exposing the MergeOperator through the Java API,
    * so in the meantime, rCache uses locks at key level to perform a
    * Read-Modify-Write in an atomic fashion.
    */
  private[this] val lazyWeakLock: Striped[Lock] = Striped.lazyWeakLock(256)

  override def get[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                       valueDecoder: ColumnDecoder[A],
                                                       ec: ExecutionContext): Operation[Column[A]] = Future {
    val encodedKey: Array[Byte] = keyEncoder.encode(key)
    valueDecoder.decode(rocksDB.get(encodedKey)) match {
      case Some(Column(_, _, _, Some(ttl))) if timestamp.getEpochSecond >= ttl.getEpochSecond =>
        rocksDB.delete(encodedKey)
        Left(KeyNotFound(s"key /${key.value} not present"))
      case Some(column) => Right(column)
      case None         => Left(KeyNotFound(s"key /${key.value} not present"))
    }
  }

  override def put[A](key: Key[A], column: Column[A])(implicit keyEncoder: KeyEncoder[A],
                                                      valueFormat: ColumnCodec[A],
                                                      ec: ExecutionContext): Operation[Unit] = async {
    val encodedKey: Array[Byte] = keyEncoder.encode(key)
    lockByKey(key) {
      if (isColumnNonExistingOrInThePast(encodedKey)(column.timestamp)) {
        val encodedColumn = valueFormat.encode(column)
        rocksDB.put(encodedKey, encodedColumn)
      }
    }
  }

  override def delete[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                          valueReader: ColumnCodec[A],
                                                          ec: ExecutionContext): Operation[Unit] = async {
    val encodedKey: Array[Byte] = keyEncoder.encode(key)
    lockByKey(key) {
      if (isColumnNonExistingOrInThePast(encodedKey)(timestamp)) {
        rocksDB.delete(encodedKey)
      }
    }
  }

  private[this] def isColumnNonExistingOrInThePast[A](encodedKey: Array[Byte])(timestamp: Instant)(
      implicit valueDecoder: ColumnDecoder[A]): Boolean = {
    val column = valueDecoder.decode(rocksDB.get(encodedKey))
    column.isEmpty || column.exists(_.timestamp.getEpochSecond < timestamp.getEpochSecond)
  }

  private[this] def async[A](block: => A)(implicit ec: ExecutionContext): Operation[A] =
    Future(Try(block).toEither.left.map(t => StorageError(t.getMessage)))

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
