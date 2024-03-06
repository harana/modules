package com.harana.modules.salesforce

import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.salesforce.models.{SalesforceError, SalesforceQuota}
import io.circe.Json
import io.circe.optics.JsonPath
import zio.{IO, ZIO, ZLayer}

object LiveSalesforce {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveSalesforce(config, http, logger, micrometer)
  }
}

case class LiveSalesforce(config: Config, http: Http, logger: Logger, micrometer: Micrometer) extends Salesforce {

  private val loginTokenUrl = "https://login.salesforce.com/services/oauth2/token"

  private val accessToken =
    for {
      username      <- config.secret("salesforce-username")
      password      <- config.secret("salesforce-password")
      clientId      <- config.secret("salesforce-client-id")
      clientSecret  <- config.secret("salesforce-client-secret")
      securityToken <- config.secret("salesforce-security-token")
      grantType     <- config.string("salesforce.grantType")
      response      <- http.postAsJson(loginTokenUrl, params = Map(
                        "grant_type"    -> List(grantType),
                        "client_id"     -> List(clientId),
                        "client_secret" -> List(clientSecret),
                        "username"      -> List(username),
                        "password"      -> List(s"$password$securityToken")
                      )).mapBoth(SalesforceError.ConnectionError, JsonPath.root.access_token.string.getOption)
    } yield response


  def quota: IO[SalesforceError, SalesforceQuota] =
    null
//      for {
//        baseUrl <- config.string("salesforce.baseUrl")
//        apiVersion <- config.int("salesforce.apiVersion")
//        json <- get(s"$baseUrl/services/data/v$apiVersion/limits")
//        response <- (json \ "DailyApiRequests").toOption match {
//            case Some(value) =>
//              val max = (value \ "Max").toString.toFloat
//              val remaining = (value \ "Remaining").toString.toFloat
//              val used = max - remaining
//              val percent: Float = (used / max) * 100
//              IO.succeed(SalesforceQuota(used.toInt, remaining.toInt, percent.toInt))
//
//            case None =>
//              IO.fail(SalesforceError.ParseError)
//          }
//        } yield response


  def describeObject(name: String): IO[SalesforceError, Json] =
    for {
      baseUrl     <- config.string("salesforce.baseUrl")
      apiVersion  <- config.int("salesforce.apiVersion")
      json        <- get(s"$baseUrl/services/data/v$apiVersion/sobjects/$name/describe")
    } yield json


  def objectList: IO[SalesforceError, Json] =
    null
//      for {
//        baseUrl     <- config.string("salesforce.baseUrl")
//        apiVersion  <- config.int("salesforce.apiVersion")
//        json        <- get(s"$baseUrl/services/data/v$apiVersion/sobjects")
//        response    <- (json \ "sobjects") match {
//          case JArray(x) => IO.succeed(x)
//          case _ => IO.fail(SalesforceError.ParseError)
//        }
//      } yield response


  def objectNames: IO[SalesforceError, List[String]] = {
    null
  }
//      objectList.map(_.map {
//        item => (item \ "name").toString
//      })
//    }


  private def get(url: String, query: Option[String] = None): IO[SalesforceError, Json] = {
    accessToken.flatMap { token =>
      val headers = Map(
        "Authorization" -> s"Bearer $token",
        "Content-Type" -> "application/json"
      )
      val params = query.map { q => Map("q" -> List(q)) }.getOrElse(Map())
      http.getAsJson(url, params, headers).mapError(SalesforceError.ConnectionError)
    }
  }
}