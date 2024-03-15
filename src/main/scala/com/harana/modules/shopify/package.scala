package com.harana.modules

import com.harana.modules.shopify.models._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.util.Try

package object shopify {

  case class Output(productTitle: String,
                    variantTitle: String,
                    variantSku: String,
                    variantId: Long,
                    variantOption1: String,
                    variantOption2: String,
                    variantOption3: String,
                    month1Sales: String,
                    month2Sales: String,
                    month3Sales: String,
                    totalSales: String,
                    inventoryLevel: String)

  implicit val jsonConfig: Configuration = Configuration.default.withSnakeCaseMemberNames.withSnakeCaseConstructorNames.withDefaults

  implicit val encodeInstant: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)
  implicit val decodeInstant: Decoder[Instant] = Decoder.decodeString.emapTry { str => Try(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(str))) }

  implicit val addressEncoder: Encoder[Address] = deriveConfiguredEncoder[Address]
  implicit val addressDecoder: Decoder[Address] = deriveConfiguredDecoder[Address]
  implicit val attributeEncoder: Encoder[Attribute] = deriveConfiguredEncoder[Attribute]
  implicit val attributeDecoder: Decoder[Attribute] = deriveConfiguredDecoder[Attribute]
  implicit val customerEncoder: Encoder[Customer] = deriveConfiguredEncoder[Customer]
  implicit val customerDecoder: Decoder[Customer] = deriveConfiguredDecoder[Customer]
  implicit val fulfilmentEncoder: Encoder[Fulfilment] = deriveConfiguredEncoder[Fulfilment]
  implicit val fulfilmentDecoder: Decoder[Fulfilment] = deriveConfiguredDecoder[Fulfilment]
  implicit val imageEncoder: Encoder[Image] = deriveConfiguredEncoder[Image]
  implicit val imageDecoder: Decoder[Image] = deriveConfiguredDecoder[Image]
  implicit val inventoryLevelEncoder: Encoder[InventoryLevel] = deriveConfiguredEncoder[InventoryLevel]
  implicit val inventoryLevelDecoder: Decoder[InventoryLevel] = deriveConfiguredDecoder[InventoryLevel]
  implicit val inventoryPolicyEncoder: Encoder[InventoryPolicy] = deriveConfiguredEncoder[InventoryPolicy]
  implicit val inventoryPolicyDecoder: Decoder[InventoryPolicy] = deriveConfiguredDecoder[InventoryPolicy]
  implicit val lineItemEncoder: Encoder[LineItem] = deriveConfiguredEncoder[LineItem]
  implicit val lineItemDecoder: Decoder[LineItem] = deriveConfiguredDecoder[LineItem]
  implicit val locationEncoder: Encoder[Location] = deriveConfiguredEncoder[Location]
  implicit val locationDecoder: Decoder[Location] = deriveConfiguredDecoder[Location]
  implicit val metafieldEncoder: Encoder[Metafield] = deriveConfiguredEncoder[Metafield]
  implicit val metafieldDecoder: Decoder[Metafield] = deriveConfiguredDecoder[Metafield]
  implicit val metaobjectEncoder: Encoder[Metaobject] = deriveConfiguredEncoder[Metaobject]
  implicit val metaobjectDecoder: Decoder[Metaobject] = deriveConfiguredDecoder[Metaobject]
  implicit val metaobjectDefinitionEncoder: Encoder[MetaobjectDefinition] = deriveConfiguredEncoder[MetaobjectDefinition]
  implicit val metaobjectDefinitionDecoder: Decoder[MetaobjectDefinition] = deriveConfiguredDecoder[MetaobjectDefinition]
  implicit val metafieldTypeEncoder: Encoder[MetafieldType] = deriveConfiguredEncoder[MetafieldType]
  implicit val metafieldTypeDecoder: Decoder[MetafieldType] = deriveConfiguredDecoder[MetafieldType]
  implicit val orderEncoder: Encoder[Order] = deriveConfiguredEncoder[Order]
  implicit val orderDecoder: Decoder[Order] = deriveConfiguredDecoder[Order]
  implicit val productEncoder: Encoder[Product] = deriveConfiguredEncoder[Product]
  implicit val productDecoder: Decoder[Product] = deriveConfiguredDecoder[Product]
  implicit val productOptionEncoder: Encoder[ProductOption] = deriveConfiguredEncoder[ProductOption]
  implicit val productOptionDecoder: Decoder[ProductOption] = deriveConfiguredDecoder[ProductOption]
  implicit val productVariantEncoder: Encoder[ProductVariant] = deriveConfiguredEncoder[ProductVariant]
  implicit val productVariantDecoder: Decoder[ProductVariant] = deriveConfiguredDecoder[ProductVariant]
  implicit val refundEncoder: Encoder[Refund] = deriveConfiguredEncoder[Refund]
  implicit val refundDecoder: Decoder[Refund] = deriveConfiguredDecoder[Refund]
  implicit val refundLineItemEncoder: Encoder[RefundLineItem] = deriveConfiguredEncoder[RefundLineItem]
  implicit val refundLineItemDecoder: Decoder[RefundLineItem] = deriveConfiguredDecoder[RefundLineItem]
  implicit val refundShippingDetailsEncoder: Encoder[RefundShippingDetails] = deriveConfiguredEncoder[RefundShippingDetails]
  implicit val refundShippingDetailsDecoder: Decoder[RefundShippingDetails] = deriveConfiguredDecoder[RefundShippingDetails]
  implicit val shippingLineEncoder: Encoder[ShippingLine] = deriveConfiguredEncoder[ShippingLine]
  implicit val shippingLineDecoder: Decoder[ShippingLine] = deriveConfiguredDecoder[ShippingLine]
  implicit val taxLineEncoder: Encoder[TaxLine] = deriveConfiguredEncoder[TaxLine]
  implicit val taxLineDecoder: Decoder[TaxLine] = deriveConfiguredDecoder[TaxLine]
  implicit val transactionEncoder: Encoder[Transaction] = deriveConfiguredEncoder[Transaction]
  implicit val transactionDecoder: Decoder[Transaction] = deriveConfiguredDecoder[Transaction]
  implicit val transactionReceiptEncoder: Encoder[TransactionReceipt] = deriveConfiguredEncoder[TransactionReceipt]
  implicit val transactionReceiptDecoder: Decoder[TransactionReceipt] = deriveConfiguredDecoder[TransactionReceipt]
}
