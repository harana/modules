package com.harana.modules.google

import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.{Task, ZIO, ZLayer}

object LiveGoogle {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveGoogle(config, http, logger, micrometer)
  }
}

case class LiveGoogle(config: Config, http: Http, logger: Logger, micrometer: Micrometer) extends Google {

    def pageView(clientId: String, page: String, title: String): Task[Event] =
      for {
        propertyId      <- config.string("google.tags.propertyId")
        domain          <- config.string("http.domain", "domain")
        event           <- ZIO.attempt(
                              Map(
                                "v" -> 1,
                                "tid" -> propertyId,
                                "cid" -> clientId,
                                "t" -> "pageview",
                                "dh" -> domain,
                                "dp" -> (if (page.startsWith("/")) page else s"/$page"),
                                "dt" -> title,
                              ).mkString("&")
                            )
      } yield event


    def event(clientId: String, category: String, action: String, label: String, value: String): Task[Event] =
      for {
        propertyId      <- config.string("google.tags.propertyId")
        event           <- ZIO.attempt(
                              Map(
                                "v" -> 1,
                                "tid" -> propertyId,
                                "cid" -> clientId,
                                "t" -> "event",
                                "ec" -> category,
                                "ea" -> action,
                                "el" -> label,
                                "ev" -> value
                              ).mkString("&")
                            )
      } yield event


    def exception(clientId: String, description: String, fatal: Boolean): Task[Event] =
      for {
          propertyId      <- config.string("google.tags.propertyId")
          event           <- ZIO.attempt(
                              Map(
                                "v" -> 1,
                                "tid" -> propertyId,
                                "cid" -> clientId,
                                "t" -> "event",
                                "exd" -> description,
                                "exf" -> (if (fatal) 1 else 0)
                              ).mkString("&")
                            )
      } yield event


    def time(clientId: String, category: String, variable: String, time: Long, label: String): Task[Event] =
      for {
          propertyId      <- config.string("google.tags.propertyId")
          event           <- ZIO.attempt(
                              Map(
                                "v" -> 1,
                                "tid" -> propertyId,
                                "cid" -> clientId,
                                "t" -> "timing",
                                "utc" -> category,
                                "utv" -> variable ,
                                "utt" -> time,
                                "utl" -> label
                              ).mkString("&")
                            )
      } yield event


    def send(event: Event): Task[Unit] =
      for {
          url             <- config.string("google.tags.url")
          _               <- http.post(s"$url/collect", Some(event)).mapError(e => new Exception(e.toString))
      } yield ()


    def batch(events: List[Event]): Task[Unit] =
      for {
          url             <- config.string("google.tags.url")
          _               <- http.post(s"$url/batch", Some(events.mkString("\n"))).mapError(e => new Exception(e.toString))
      } yield ()
}