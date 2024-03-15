package com.harana.modules.shopify

import com.harana.modules.shopify.models._
import io.circe.Decoder
import zio.Task
import zio.macros.accessible

@accessible
trait Shopify {

    def all[T](fn: => Task[Page[T]])(implicit connection: ShopifyConnection, d: Decoder[T]): Task[List[T]]

    def previousPage[T](page: Page[T])(implicit connection: ShopifyConnection, d: Decoder[T]): Task[Option[Page[T]]]

    def nextPage[T](page: Page[T])(implicit connection: ShopifyConnection, d: Decoder[T]): Task[Option[Page[T]]]

    def forecastInventory(implicit connection: ShopifyConnection): Task[List[Output]]

    def orders(ids: List[String] = List(),
               limit: Option[Int] = None,
               sinceId: Option[String] = None,
               createdAtMin: Option[String] = None,
               createdAtMax: Option[String] = None,
               updatedAtMin: Option[String] = None,
               updatedAtMax: Option[String] = None,
               processedAtMin: Option[String] = None,
               processedAtMax: Option[String] = None,
               attributionAppId: Option[String] = None,
               status: Option[String] = None,
               financialStatus: Option[String] = None,
               fulfillment_status: Option[String] = None,
               fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[Order]]

    def products(ids: List[String] = List(),
                 limit: Option[Int] = None,
                 sinceId: Option[String] = None,
                 title: Option[String] = None,
                 vendor: Option[String] = None,
                 handle: Option[String] = None,
                 productType: Option[String] = None,
                 status: Option[String] = None,
                 collectionId: Option[String] = None,
                 createdAtMin: Option[String] = None,
                 createdAtMax: Option[String] = None,
                 updatedAtMin: Option[String] = None,
                 updatedAtMax: Option[String] = None,
                 processedAtMin: Option[String] = None,
                 processedAtMax: Option[String] = None,
                 publishedStatus: Option[String] = None,
                 fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[models.Product]]

    def inventoryLevels(inventoryItemIds: List[Long] = List(),
                        locationIds: List[Long] = List(),
                        limit: Option[Int] = None,
                        updatedAtMin: Option[String] = None)(implicit connection: ShopifyConnection): Task[Page[InventoryLevel]]

    def customer(id: String, fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Customer]

    def customerMetafield(customerId: String, metafieldId: String)(implicit connection: ShopifyConnection): Task[Metafield]

    def createCustomerMetafield(customerId: String, metafield: Metafield)(implicit connection: ShopifyConnection): Task[Metafield]

    def setCustomerMetafield(customerId: String, metafieldId: String, metafieldType: String, metafieldValue: String)(implicit connection: ShopifyConnection): Task[Unit]

    def orderMetafield(orderId: String, metafieldId: String)(implicit connection: ShopifyConnection): Task[Metafield]

    def setOrderMetafield(orderId: String, metafieldId: String, metafieldType: String, metafieldValue: String)(implicit connection: ShopifyConnection): Task[Unit]

    def locations(implicit connection: ShopifyConnection): Task[Page[Location]]

    def product(id: String, fields: List[String] = List())(implicit connection: ShopifyConnection): Task[models.Product]

    def productVariants(productId: Long,
                        limit: Option[Int] = None,
                        presentmentCurrencies: List[String] = List(),
                        sinceId: Option[String] = None,
                        fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[ProductVariant]]

    def uploadImage(sourceUrl: String)(implicit connection: ShopifyConnection): Task[Unit]
}