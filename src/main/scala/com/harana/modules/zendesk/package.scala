package com.harana.modules

import com.harana.modules.zendesk.models.ZendeskError
import org.asynchttpclient.ListenableFuture
import org.zendesk.client.v2.model.JobStatus
import org.zendesk.client.v2.{ZendeskResponseException, ZendeskResponseRateLimitException}
import zio.{IO, Task, ZIO}

import scala.compat.java8.FutureConverters._
import scala.jdk.CollectionConverters._

package object zendesk {

  implicit def iterableTaskToIO[A](fn: zio.Task[java.lang.Iterable[A]]): IO[ZendeskError, List[A]] =
    fn.mapBoth(handleException, _.asScala.toList)

  implicit def taskToIO[A](fn: zio.Task[A]): IO[ZendeskError, A] =
    fn.mapError(handleException)

  implicit def futureToIO[A](fn: Task[ListenableFuture[A]]): IO[ZendeskError, A] =
    fn.flatMap { f =>
      ZIO.fromFuture { implicit ec =>
        f.toCompletableFuture.toScala
      }
    }.mapError(handleException)

  implicit def listenableFutureToIO[A](fn: Task[ListenableFuture[JobStatus]]): IO[ZendeskError, List[A]] =
    listenableFutureToIO(fn)

  implicit def javaListToScalaList[A](list: java.util.List[A]): List[A] =
    list.asScala.toList

  implicit def scalaListToJavaIterable[A](list: List[A]): java.util.List[A] =
    list.asJava

  def handleException(t: Throwable): ZendeskError =
    t match {
      case e: ZendeskResponseRateLimitException => ZendeskError.RateLimit(e)
      case e: ZendeskResponseException => ZendeskError.Response(e)
      case t: Throwable => ZendeskError.Unknown(t)
    }
}