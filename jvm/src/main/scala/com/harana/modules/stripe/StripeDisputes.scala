package com.harana.modules.stripe

import com.outr.stripe.dispute.{Dispute, DisputeEvidence}
import com.outr.stripe.{QueryConfig, ResponseError, StripeList, TimestampFilter}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeDisputes {
   def byId(disputeId: String): IO[ResponseError, Dispute]

    def update(disputeId: String,
               evidence: Option[DisputeEvidence] = None,
               metadata: Map[String, String]): IO[ResponseError, Dispute]

    def close(disputeId: String): IO[ResponseError, Dispute]

    def list(created: Option[TimestampFilter] = None,
             config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[Dispute]]
}