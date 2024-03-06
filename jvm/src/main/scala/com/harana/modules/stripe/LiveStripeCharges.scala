package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.charge.{Charge, FraudDetails, Shipping}
import zio.{IO, ZIO, ZLayer}

import scala.language.implicitConversions

object LiveStripeCharges {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeCharges(config, logger, micrometer)
  }
}

case class LiveStripeCharges(config: Config, logger: Logger, micrometer: Micrometer) extends StripeCharges {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).charges)

  def create(amount: Money,
             currency: String,
             applicationFee: Option[Money] = None,
             capture: Boolean = true,
             description: Option[String] = None,
             destination: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             receiptEmail: Option[String] = None,
             shipping: Option[Shipping] = None,
             customer: Option[String] = None,
             source: Option[String] = None,
             statementDescriptor: Option[String] = None): IO[ResponseError, Charge] =
    for {
      c <- client
      r <- execute(c.create(amount, currency, applicationFee, capture, description, destination, metadata, receiptEmail, shipping,
        customer, source, statementDescriptor))
    } yield r


  def byId(chargeId: String): IO[ResponseError, Charge] =
    for {
      c <- client
      r <- execute(c.byId(chargeId))
    } yield r


  def update(chargeId: String,
             description: Option[String] = None,
             fraudDetails: Option[FraudDetails] = None,
             metadata: Map[String, String] = Map.empty,
             receiptEmail: Option[String] = None,
             shipping: Option[Shipping] = None): IO[ResponseError, Charge] =
    for {
      c <- client
      r <- execute(c.update(chargeId, description, fraudDetails, metadata, receiptEmail, shipping))
    } yield r


  def capture(chargeId: String,
              amount: Option[Money] = None,
              applicationFee: Option[Money] = None,
              receiptEmail: Option[String] = None,
              statementDescriptor: Option[String] = None): IO[ResponseError, Charge] =
    for {
      c <- client
      r <- execute(c.capture(chargeId, amount, applicationFee, receiptEmail, statementDescriptor))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           customer: Option[String] = None,
           source: Option[String] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Charge]] =
    for {
      c <- client
      r <- execute(c.list(created, customer, source, config))
    } yield r

}