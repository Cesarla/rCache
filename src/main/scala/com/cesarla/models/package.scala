package com.cesarla

import cats.data.EitherT
import cats.implicits._
import com.cesarla.models.Operation.toOperation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

package object models {
  type Operation[A] = EitherT[Future, Problem, A]

  object Operation {
    implicit def toOperation[A](future: Future[Either[Problem, A]]): Operation[A] =
      EitherT[Future, Problem, A](future)
  }

  object OperationsOps {

    implicit class RichOperation[A](operation: Operation[A]) {
      def isSuccess: Future[Boolean] = operation.isRight

      def isFailure: Future[Boolean] = operation.isLeft

      def await(implicit duration: Duration): Either[Problem, A] = Await.result(operation.value, duration)
    }

    implicit def toOperationSuccessOps[A](value: A): OperationSuccessOps[A] = new OperationSuccessOps[A](value)

    final class OperationSuccessOps[A](val value: A) extends AnyVal {
      def asSuccess: Operation[A] = Future.successful(Right(value))
    }

    implicit def toOperationFailureOps[A](problem: Problem): OperationFailureOps[A] =
      new OperationFailureOps[A](problem)

    final class OperationFailureOps[A](val problem: Problem) extends AnyVal {
      def asFailure: Operation[A] = Future.successful(Left(problem))
    }

  }
}
