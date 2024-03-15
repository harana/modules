package com.harana.modules.stripe

import com.outr.stripe.transfer.TransferReversal
import com.outr.stripe.{Money, QueryConfig, ResponseError, StripeList}
import zio.IO
import zio.macros.accessible

@accessible
trait StripeTransferReversals {
  def create(transferId: String,
             amount: Option[Money] = None,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty,
             refundApplicationFee: Boolean = false): IO[ResponseError, TransferReversal]

  def byId(transferId: String, transferReversalId: String): IO[ResponseError, TransferReversal]

  def update(transferId: String,
             transferReversalId: String,
             description: Option[String] = None,
             metadata: Map[String, String] = Map.empty): IO[ResponseError, TransferReversal]

  def list(transferId: String,
           config: QueryConfig = QueryConfig.default): IO[ResponseError, StripeList[TransferReversal]]
}