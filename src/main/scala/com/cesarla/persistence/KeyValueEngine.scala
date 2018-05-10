package com.cesarla.persistence

import java.time.Instant

import com.cesarla.models.{Column, Key}

import scala.concurrent.{ExecutionContext, Future}

trait KeyValueEngine {

  type KeyCodec[A] = Codec[Key[A]]
  type KeyDecoder[A] = Decoder[Key[A]]
  type KeyEncoder[A] = Encoder[Key[A]]

  type ColumnCodec[A] = Codec[Column[A]]
  type ColumnDecoder[A] = Decoder[Column[A]]
  type ColumnEncoder[A] = Encoder[Column[A]]

  def get[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                              valueDecoder: ColumnDecoder[A],
                                              ec: ExecutionContext): Future[Option[Column[A]]]

  def put[A](key: Key[A], column: Column[A])(implicit keyEncoder: KeyEncoder[A],
                                             valueFormat: ColumnCodec[A],
                                             ec: ExecutionContext): Future[Unit]

  def delete[A](key: Key[A], timestamp: Instant)(implicit keyEncoder: KeyEncoder[A],
                                                 valueReader: ColumnCodec[A],
                                                 ec: ExecutionContext): Future[Unit]
}
