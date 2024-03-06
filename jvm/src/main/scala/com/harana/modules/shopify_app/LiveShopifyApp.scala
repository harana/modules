package com.harana.modules.shopify_app

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.shopify.ShopifySdk
import com.shopify.model._
import org.joda.time.DateTime
import zio.{Task, ZIO, ZLayer}

import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

object LiveShopifyApp {
	val layer = ZLayer {
		for {
			config        <- ZIO.service[Config]
			logger        <- ZIO.service[Logger]
			micrometer    <- ZIO.service[Micrometer]
		} yield LiveShopifyApp(config, logger, micrometer)
	}
}

case class LiveShopifyApp(config: Config, logger: Logger, micrometer: Micrometer) extends ShopifyApp {

	def activateRecurringApplicationCharge(subdomain: String, accessToken: String, chargeId: String): Task[ShopifyRecurringApplicationCharge] =
		sdk(subdomain, accessToken).map(_.activateRecurringApplicationCharge(chargeId))

	def cancelFulfillment(subdomain: String, accessToken: String, orderId: String, fulfillmentId: String): Task[ShopifyFulfillment] =
		sdk(subdomain, accessToken).map(_.cancelFulfillment(orderId, fulfillmentId))

	def cancelOrder(subdomain: String, accessToken: String, orderId: String, reason: String): Task[ShopifyOrder] =
		sdk(subdomain, accessToken).map(_.cancelOrder(orderId, reason))

	def closeOrder(subdomain: String, accessToken: String, orderId: String): Task[ShopifyOrder] =
		sdk(subdomain, accessToken).map(_.closeOrder(orderId))

	def createCustomCollection(subdomain: String, accessToken: String, request: ShopifyCustomCollectionCreationRequest): Task[ShopifyCustomCollection] =
		sdk(subdomain, accessToken).map(_.createCustomCollection(request))

	def createFulfillment(subdomain: String, accessToken: String, request: ShopifyFulfillmentCreationRequest): Task[ShopifyFulfillment] =
		sdk(subdomain, accessToken).map(_.createFulfillment(request))

	def createGiftCard(subdomain: String, accessToken: String, request: ShopifyGiftCardCreationRequest): Task[ShopifyGiftCard] =
		sdk(subdomain, accessToken).map(_.createGiftCard(request))

	def createOrder(subdomain: String, accessToken: String, request: ShopifyOrderCreationRequest): Task[ShopifyOrder] =
		sdk(subdomain, accessToken).map(_.createOrder(request))

	def createProduct(subdomain: String, accessToken: String, request: ShopifyProductCreationRequest): Task[ShopifyProduct] =
		sdk(subdomain, accessToken).map(_.createProduct(request))

	def createProductMetafield(subdomain: String, accessToken: String, request: ShopifyProductMetafieldCreationRequest): Task[Metafield] =
		sdk(subdomain, accessToken).map(_.createProductMetafield(request))

	def createRecurringApplicationCharge(subdomain: String, accessToken: String, request: ShopifyRecurringApplicationChargeCreationRequest): Task[ShopifyRecurringApplicationCharge] =
		sdk(subdomain, accessToken).map(_.createRecurringApplicationCharge(request))

	def createVariantMetafield(subdomain: String, accessToken: String, request: ShopifyVariantMetafieldCreationRequest): Task[Metafield] =
		sdk(subdomain, accessToken).map(_.createVariantMetafield(request))

	def deleteProduct(subdomain: String, accessToken: String, productId: String): Task[Boolean] =
		sdk(subdomain, accessToken).map(_.deleteProduct(productId))

	def getAccessToken(subdomain: String, accessToken: String): Task[String] =
		sdk(subdomain, accessToken).map(_.getAccessToken)

	def getCustomCollections(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyCustomCollection]] =
		sdk(subdomain, accessToken).map(_.getCustomCollections(pageInfo, pageSize))

	def getCustomCollections(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyCustomCollection]] =
		sdk(subdomain, accessToken).map(_.getCustomCollections(pageSize))

	def getCustomCollections(subdomain: String, accessToken: String): Task[List[ShopifyCustomCollection]] =
		sdk(subdomain, accessToken).map(_.getCustomCollections.asScala.toList)

	def getCustomer(subdomain: String, accessToken: String, customerId: String): Task[ShopifyCustomer] =
		sdk(subdomain, accessToken).map(_.getCustomer(customerId))

	def getCustomers(subdomain: String, accessToken: String, request: ShopifyGetCustomersRequest): Task[ShopifyPage[ShopifyCustomer]] =
		sdk(subdomain, accessToken).map(_.getCustomers(request))

	def getLocations(subdomain: String, accessToken: String): Task[List[ShopifyLocation]] =
		sdk(subdomain, accessToken).map(_.getLocations.asScala.toList)

	def getOrder(subdomain: String, accessToken: String, orderId: String): Task[ShopifyOrder] =
		sdk(subdomain, accessToken).map(_.getOrder(orderId))

	def getOrderMetafields(subdomain: String, accessToken: String, orderId: String): Task[List[Metafield]] =
		sdk(subdomain, accessToken).map(_.getOrderMetafields(orderId).asScala.toList)

	def getOrderRisks(subdomain: String, accessToken: String, orderId: String): Task[List[ShopifyOrderRisk]] =
		sdk(subdomain, accessToken).map(_.getOrderRisks(orderId).asScala.toList)

	def getOrderTransactions(subdomain: String, accessToken: String, orderId: String): Task[List[ShopifyTransaction]] =
		sdk(subdomain, accessToken).map(_.getOrderTransactions(orderId).asScala.toList)

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate))

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime, maximumCreationDate: DateTime): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate, maximumCreationDate))

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime, maximumCreationDate: DateTime, appId: String): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate, maximumCreationDate, appId))

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime, maximumCreationDate: DateTime, appId: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate, maximumCreationDate, appId, pageSize))

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime, maximumCreationDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate, maximumCreationDate, pageSize))

	def getOrders(subdomain: String, accessToken: String, minimumCreationDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(minimumCreationDate, pageSize))

	def getOrders(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(pageInfo, pageSize))

	def getOrders(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders(pageSize))

	def getOrders(subdomain: String, accessToken: String): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getOrders)

	def getProduct(subdomain: String, accessToken: String, productId: String): Task[ShopifyProduct] =
		sdk(subdomain, accessToken).map(_.getProduct(productId))

	def getProductCount(subdomain: String, accessToken: String): Task[Int] =
		sdk(subdomain, accessToken).map(_.getProductCount)

	def getProductMetafields(subdomain: String, accessToken: String, productId: String): Task[List[Metafield]] =
		sdk(subdomain, accessToken).map(_.getProductMetafields(productId).asScala.toList)

	def getProducts(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyProduct]] =
		sdk(subdomain, accessToken).map(_.getProducts(pageInfo, pageSize))

	def getProducts(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyProduct]] =
		sdk(subdomain, accessToken).map(_.getProducts(pageSize))

	def getProducts(subdomain: String, accessToken: String): Task[ShopifyProducts] =
		sdk(subdomain, accessToken).map(_.getProducts)

	def getRecurringApplicationCharge(subdomain: String, accessToken: String, chargeId: String): Task[ShopifyRecurringApplicationCharge] =
		sdk(subdomain, accessToken).map(_.getRecurringApplicationCharge(chargeId))

	def getShop(subdomain: String, accessToken: String): Task[ShopifyShop] =
		sdk(subdomain, accessToken).map(_.getShop)

	def getUpdatedOrdersCreatedBefore(subdomain: String, accessToken: String, minimumUpdatedAtDate: DateTime, maximumUpdatedAtDate: DateTime, maximumCreatedAtDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]] =
		sdk(subdomain, accessToken).map(_.getUpdatedOrdersCreatedBefore(minimumUpdatedAtDate, maximumUpdatedAtDate, maximumCreatedAtDate, pageSize))

	def getVariant(subdomain: String, accessToken: String, variantId: String): Task[ShopifyVariant] =
		sdk(subdomain, accessToken).map(_.getVariant(variantId))

	def getVariantMetafields(subdomain: String, accessToken: String, variantId: String): Task[List[Metafield]] =
		sdk(subdomain, accessToken).map(_.getVariantMetafields(variantId).asScala.toList)

	def refund(subdomain: String, accessToken: String, request: ShopifyRefundCreationRequest): Task[ShopifyRefund] =
		sdk(subdomain, accessToken).map(_.refund(request))

	def revokeOAuthToken(subdomain: String, accessToken: String): Task[Boolean] =
		sdk(subdomain, accessToken).map(_.revokeOAuthToken)

	def searchCustomers(subdomain: String, accessToken: String, query: String): Task[ShopifyPage[ShopifyCustomer]] =
		sdk(subdomain, accessToken).map(_.searchCustomers(query))

	def updateCustomer(subdomain: String, accessToken: String, request: ShopifyCustomerUpdateRequest): Task[ShopifyCustomer] =
		sdk(subdomain, accessToken).map(_.updateCustomer(request))

	def updateFulfillment(subdomain: String, accessToken: String, request: ShopifyFulfillmentUpdateRequest): Task[ShopifyFulfillment] =
		sdk(subdomain, accessToken).map(_.updateFulfillment(request))

	def updateInventoryLevel(subdomain: String, accessToken: String, inventoryItemId: String, locationId: String, quantity: Long): Task[ShopifyInventoryLevel] =
		sdk(subdomain, accessToken).map(_.updateInventoryLevel(inventoryItemId, locationId, quantity))

	def updateOrderShippingAddress(subdomain: String, accessToken: String, request: ShopifyOrderShippingAddressUpdateRequest): Task[ShopifyOrder] =
		sdk(subdomain, accessToken).map(_.updateOrderShippingAddress(request))

	def updateProduct(subdomain: String, accessToken: String, request: ShopifyProductUpdateRequest): Task[ShopifyProduct] =
		sdk(subdomain, accessToken).map(_.updateProduct(request))

	def updateVariant(subdomain: String, accessToken: String, request: ShopifyVariantUpdateRequest): Task[ShopifyVariant] =
		sdk(subdomain, accessToken).map(_.updateVariant(request))

	private def sdk(subdomain: String, accessToken: String): Task[ShopifySdk] =
		ZIO.attempt(ShopifySdk.newBuilder().withSubdomain(subdomain).withAccessToken(accessToken).build())

}