package com.harana.modules.stripe

import com.outr.stripe._
import com.outr.stripe.subscription.Coupon
import zio.IO
import zio.macros.accessible

@accessible
trait StripeCoupons {
   def create(couponId: String,
               duration: String,
               amountOff: Option[Money] = None,
               currency: Option[String] = None,
               durationInMonths: Option[Int] = None,
               maxRedemptions: Option[Int] = None,
               metadata: Map[String, String] = Map.empty,
               percentOff: Option[Int] = None,
               redeemBy: Option[Long] = None): IO[ResponseError, Coupon]

    def byId(couponId: String): IO[ResponseError, Coupon]

    def update(couponId: String, metadata: Map[String, String]): IO[ResponseError, Coupon]

    def delete(couponId: String): IO[ResponseError, Deleted]

    def list(created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Coupon]]
}