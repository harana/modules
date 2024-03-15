package com.harana.modules

import com.harana.modules.airbyte.LiveAirbyte
import com.harana.modules.airtable.LiveAirtable
import com.harana.modules.alluxiofs.LiveAlluxioFs
import com.harana.modules.argo.LiveArgo
import com.harana.modules.aws.LiveAWS
import com.harana.modules.aws_s3.LiveAwsS3
import com.harana.modules.clearbit.LiveClearbit
import com.harana.modules.core.http.LiveHttp
import com.harana.modules.core.{Layers => CoreLayers}
import com.harana.modules.docker.LiveDocker
import com.harana.modules.email.LiveEmail
import com.harana.modules.file.LiveFile
import com.harana.modules.handlebars.LiveHandlebars
import com.harana.modules.kubernetes.LiveKubernetes
import com.harana.modules.mixpanel.LiveMixpanel
import com.harana.modules.ohc.LiveOHC
import com.harana.modules.shopify.LiveShopify
import com.harana.modules.stripe._
import com.harana.modules.thumbnailator.LiveThumbnailator
import com.harana.modules.vertx.LiveVertx
import com.harana.modules.zendesk.LiveZendesk
import zio.Clock

object Layers {

  lazy val http = CoreLayers.standard >>> LiveHttp.layer

  lazy val airtable = CoreLayers.standard >>> LiveAirtable.layer
  lazy val alluxioFs = CoreLayers.standard >>> LiveAlluxioFs.layer
  lazy val aws = CoreLayers.standard >>> LiveAWS.layer
  lazy val awsS3 = CoreLayers.standard >>> LiveAwsS3.layer
  lazy val clearbit = (CoreLayers.standard ++ http) >>> LiveClearbit.layer
  lazy val docker = CoreLayers.standard ++ http >>> LiveDocker.layer
  lazy val email = CoreLayers.standard >>> LiveEmail.layer
  lazy val file = CoreLayers.standard >>> LiveFile.layer
  lazy val handlebars = CoreLayers.standard >>> LiveHandlebars.layer
  lazy val kubernetes = CoreLayers.standard >>> LiveKubernetes.layer
  lazy val mixpanel = CoreLayers.standard >>> LiveMixpanel.layer
  lazy val ohc = LiveOHC.layer
  lazy val shopify = CoreLayers.standard ++ http >>> LiveShopify.layer
  lazy val thumbnailator = LiveThumbnailator.layer
  lazy val vertx = CoreLayers.standard >>> LiveVertx.layer
  lazy val zendesk = CoreLayers.standard >>> LiveZendesk.layer

  lazy val airbyte = (CoreLayers.standard ++ kubernetes) >>> LiveAirbyte.layer
  lazy val argo = (CoreLayers.standard ++ kubernetes) >>> LiveArgo.layer

  lazy val stripeAccounts = CoreLayers.standard >>> LiveStripeAccounts.layer
  lazy val stripeApplicationFeeRefunds = CoreLayers.standard >>> LiveStripeApplicationFeeRefunds.layer
  lazy val stripeApplicationFees = CoreLayers.standard >>> LiveStripeApplicationFees.layer
  lazy val stripeBalance = CoreLayers.standard >>> LiveStripeBalance.layer
  lazy val stripeCharges = CoreLayers.standard >>> LiveStripeCharges.layer
  lazy val stripeCountrySpecs = CoreLayers.standard >>> LiveStripeCountrySpecs.layer
  lazy val stripeCoupons = CoreLayers.standard >>> LiveStripeCoupons.layer
  lazy val stripeCustomerBankAccount = CoreLayers.standard >>> LiveStripeCustomerBankAccounts.layer
  lazy val stripeCustomerCreditCards = CoreLayers.standard >>> LiveStripeCustomerCreditCards.layer
  lazy val stripeCustomers = CoreLayers.standard >>> LiveStripeCustomers.layer
  lazy val stripeDiscounts = CoreLayers.standard >>> LiveStripeDiscounts.layer
  lazy val stripeDisputes = CoreLayers.standard >>> LiveStripeDisputes.layer
  lazy val stripeEvents = CoreLayers.standard >>> LiveStripeEvents.layer
  lazy val stripeExternalBankAccounts = CoreLayers.standard >>> LiveStripeExternalBankAccounts.layer
  lazy val stripeExternalCreditCards = CoreLayers.standard >>> LiveStripeExternalCreditCards.layer
  lazy val stripeInvoiceItems = CoreLayers.standard >>> LiveStripeInvoiceItems .layer
  lazy val stripeInvoices = CoreLayers.standard >>> LiveStripeInvoices.layer
  lazy val stripePlans = CoreLayers.standard >>> LiveStripePlans.layer
  lazy val stripePrices = CoreLayers.standard >>> LiveStripePrices.layer
  lazy val stripeProducts = CoreLayers.standard >>> LiveStripeProducts.layer
  lazy val stripeRefunds = CoreLayers.standard >>> LiveStripeRefunds.layer
  lazy val stripeSubscriptionItems = CoreLayers.standard >>> LiveStripeSubscriptionItems.layer
  lazy val stripeSubscriptions = CoreLayers.standard >>> LiveStripeSubscriptions.layer
  lazy val stripeTokens = CoreLayers.standard >>> LiveStripeTokens.layer
  lazy val stripeTransferReversals = CoreLayers.standard >>> LiveStripeTransferReversals.layer
  lazy val stripeTransfers = CoreLayers.standard >>> LiveStripeTransfers.layer
  lazy val stripeUI = CoreLayers.standard ++ http >>> LiveStripeUI.layer

}
