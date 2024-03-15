package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.dispute.{Dispute, DisputeEvidence}
import zio.{IO, ZIO, ZLayer}

object LiveStripeDisputes {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeDisputes(config, logger, micrometer)
  }
}

case class LiveStripeDisputes(config: Config, logger: Logger, micrometer: Micrometer) extends StripeDisputes {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).disputes)

  def byId(disputeId: String): IO[ResponseError, Dispute] =
    for {
      c <- client
      r <- execute(c.byId(disputeId))
    } yield r


  def update(disputeId: String,
             evidence: Option[DisputeEvidence] = None,
             metadata: Map[String, String]): IO[ResponseError, Dispute] =
    for {
      c <- client
      r <- execute(c.update(disputeId, evidence, metadata))
    } yield r


  def close(disputeId: String): IO[ResponseError, Dispute] =
    for {
      c <- client
      r <- execute(c.close(disputeId))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Dispute]] =
    for {
      c <- client
      r <- execute(c.list(created, config))
    } yield r

}