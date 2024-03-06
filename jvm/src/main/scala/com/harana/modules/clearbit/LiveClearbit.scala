package com.harana.modules.clearbit

import com.harana.modules.clearbit.models.RiskResponse
import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import io.circe.parser._
import zio.{Task, ZLayer, ZIO}

object LiveClearbit {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveClearbit(config, http, logger, micrometer)
  }
}

case class LiveClearbit(config: Config, http: Http, logger: Logger, micrometer: Micrometer) extends Clearbit {

  def calculateRisk(emailAddress: String, ipAddress: String, firstName: String, lastName: String): Task[RiskResponse] =
    for {
      apiKey      <- config.secret("clearbit-api-key")
      _           <- logger.debug(s"Calculating risk for email: $emailAddress")
      params      =  Map("email" -> emailAddress, "given_name" -> firstName, "family_name" -> lastName, "ip" -> ipAddress)
      response    <- http.postForm("https://risk.clearbit.com/v1/calculate", params, credentials = Some((apiKey, ""))).mapError(e => new Exception(e.toString)).onError(e => logger.error(s"Failed to calculate risk: ${e.prettyPrint}"))
      risk        <- ZIO.fromEither(decode[RiskResponse](response.body().string())).onError(e => logger.error(s"Failed to decode risk to RiskResponse object: ${e.prettyPrint}"))
    } yield risk

}
