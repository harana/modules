package com.harana.modules

import com.hubspot.algebra.Result
import com.hubspot.slack.client.models.response.SlackError
import zio.{IO, ZIO}

import java.util.Optional
import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

package object slack {

  implicit def toIO[SlackError, A](fn: CompletableFuture[Result[A, SlackError]]): IO[Either[SlackError, Throwable], A] =
    ZIO.async { (cb: IO[Either[SlackError, Throwable], A] => Unit) =>
      fn.toScala.onComplete { f =>
        f.toEither match {
          case Left(t) => cb(ZIO.fail(Right(t)))
          case Right(x) => try {
            if (x.isOk) cb(ZIO.succeed(x.unwrapOrElseThrow()))
            else cb(ZIO.fail(Left(x.unwrapErrOrElseThrow())))
          } catch {
            case e: Exception => cb(ZIO.fail(Right(e)))
          }
        }
      }
    }

  implicit def toIOIterable[A](fn: java.lang.Iterable[CompletableFuture[Result[java.util.List[A], SlackError]]]): IO[Either[SlackError, Throwable], List[A]] =
    ZIO.foreach(fn.asScala.toList)(toIO).map(_.flatMap(_.asScala.toList))

  implicit def toOptionalInt(opt: Option[Int]): Optional[Integer] =
    opt.map { o => new Integer(o) }.asJava

  implicit def toOptionalDefault[A](opt: Option[A]): Optional[A] =
    opt.asJava
}
