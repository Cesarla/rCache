package com.cesarla.persistence

import java.time.Instant
import java.util.concurrent.locks.Lock

import com.cesarla.models.{Column, Key}
import com.google.common.util.concurrent.Striped
import org.rocksdb.{Options, RocksDB}

import scala.concurrent.{ExecutionContext, Future}

class RocksDBEngine(rocksDB: RocksDB) extends KeyValueEngine {

  /**
    * RocksDB doesn't expose the MergeOperator in the Java API, so until then,
    * rCache uses locks at key level to perform a Read-Modify-Write in an
    * atomic fashion.
    */
  private[this] val lazyWeakLock: Striped[Lock] = Striped.lazyWeakLock(256)

  override def get[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                       valueDecoder: ColumnDecoder[A],
                                                       ec: ExecutionContext): Future[Option[Column[A]]] = Future {
    val encodedKey = keyEncoder.encode(key)
    Option(rocksDB.get(encodedKey)).flatMap(r => valueDecoder.decode(r))
  }

  override def put[A](key: Key[A], column: Column[A])(implicit keyEncoder: KeyEncoder[A],
                                                      valueFormat: ColumnCodec[A],
                                                      ec: ExecutionContext): Future[Unit] = Future {
    val encodedKey = keyEncoder.encode(key)
    lockByKey(key) {
      Option(rocksDB.get(encodedKey)).flatMap(r => valueFormat.decode(r)) match {
        case Some(Column(_, _, prevTimestamp, _)) if prevTimestamp.getNano > column.timestamp.getNano => ()
        case _ =>
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
      Option(rocksDB.get(encodedKey)).flatMap(r => valueReader.decode(r)) match {
        case Some(Column(_, _, prevTimestamp, _)) if prevTimestamp.getNano > timestamp.getNano => ()
        case _                                                                                 => rocksDB.delete(encodedKey)
      }
    }
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
