package com.harana.modules.stripe

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.outr.stripe._
import com.outr.stripe.subscription.Coupon
import zio.{IO, ZIO, ZLayer}

object LiveStripeCoupons {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveStripeCoupons(config, logger, micrometer)
  }
}

case class LiveStripeCoupons(config: Config, logger: Logger, micrometer: Micrometer) extends StripeCoupons {

  private val client = config.secret("stripe-secret-key").map(key => new Stripe(key).coupons)

  def create(couponId: String,
             duration: String,
             amountOff: Option[Money] = None,
             currency: Option[String] = None,
             durationInMonths: Option[Int] = None,
             maxRedemptions: Option[Int] = None,
             metadata: Map[String, String] = Map.empty,
             percentOff: Option[Int] = None,
             redeemBy: Option[Long] = None): IO[ResponseError, Coupon] =
    for {
      c <- client
      r <- execute(c.create(couponId, duration, amountOff, currency, durationInMonths, maxRedemptions, metadata, percentOff, redeemBy))
    } yield r


  def byId(couponId: String): IO[ResponseError, Coupon] =
    for {
      c <- client
      r <- execute(c.byId(couponId))
    } yield r


  def update(couponId: String, metadata: Map[String, String]): IO[ResponseError, Coupon] =
    for {
      c <- client
      r <- execute(c.update(couponId, metadata))
    } yield r


  def delete(couponId: String): IO[ResponseError, Deleted] =
    for {
      c <- client
      r <- execute(c.delete(couponId))
    } yield r


  def list(created: Option[TimestampFilter] = None,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Coupon]] =
    for {
      c <- client
      r <- execute(c.list(created, config))
    } yield r

}