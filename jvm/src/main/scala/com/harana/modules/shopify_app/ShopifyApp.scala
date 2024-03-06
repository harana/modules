package com.harana.modules.shopify_app

import com.shopify.model._
import org.joda.time.DateTime
import zio.Task
import zio.macros.accessible

@accessible
trait ShopifyApp {
    def activateRecurringApplicationCharge(subdomain: String, accessToken: String, chargeId: String): Task[ShopifyRecurringApplicationCharge]

    def cancelFulfillment(subdomain: String, accessToken: String, orderId: String, fulfillmentId: String): Task[ShopifyFulfillment]

    def cancelOrder(subdomain: String, accessToken: String, orderId: String, reason: String): Task[ShopifyOrder]

    def closeOrder(subdomain: String, accessToken: String, orderId: String): Task[ShopifyOrder]

    def createCustomCollection(subdomain: String, accessToken: String, request: ShopifyCustomCollectionCreationRequest): Task[ShopifyCustomCollection]

    def createFulfillment(subdomain: String, accessToken: String, request: ShopifyFulfillmentCreationRequest): Task[ShopifyFulfillment]

    def createGiftCard(subdomain: String, accessToken: String, request: ShopifyGiftCardCreationRequest): Task[ShopifyGiftCard]

    def createOrder(subdomain: String, accessToken: String, request: ShopifyOrderCreationRequest): Task[ShopifyOrder]

    def createProduct(subdomain: String, accessToken: String, request: ShopifyProductCreationRequest): Task[ShopifyProduct]

    def createProductMetafield(subdomain: String, accessToken: String, request: ShopifyProductMetafieldCreationRequest): Task[Metafield]

    def createRecurringApplicationCharge(subdomain: String, accessToken: String, request: ShopifyRecurringApplicationChargeCreationRequest): Task[ShopifyRecurringApplicationCharge]

    def createVariantMetafield(subdomain: String, accessToken: String, request: ShopifyVariantMetafieldCreationRequest): Task[Metafield]

    def deleteProduct(subdomain: String, accessToken: String, productId: String): Task[Boolean]

    def getAccessToken(subdomain: String, accessToken: String): Task[String]

    def getCustomCollections(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyCustomCollection]]

    def getCustomCollections(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyCustomCollection]]

    def getCustomCollections(subdomain: String, accessToken: String): Task[List[ShopifyCustomCollection]]

    def getCustomer(subdomain: String, accessToken: String, customerId: String): Task[ShopifyCustomer]

    def getCustomers(subdomain: String, accessToken: String, request: ShopifyGetCustomersRequest): Task[ShopifyPage[ShopifyCustomer]]

    def getLocations(subdomain: String, accessToken: String): Task[List[ShopifyLocation]]

    def getOrder(subdomain: String, accessToken: String, orderId: String): Task[ShopifyOrder]

    def getOrderMetafields(subdomain: String, accessToken: String, orderId: String): Task[List[Metafield]]

    def getOrderRisks(subdomain: String, accessToken: String, orderId: String): Task[List[ShopifyOrderRisk]]

    def getOrderTransactions(subdomain: String, accessToken: String, orderId: String): Task[List[ShopifyTransaction]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime, maximumCreationDate: DateTime): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime, maximumCreationDate: DateTime, appId: String): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime, maximumCreationDate: DateTime, appId: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime, maximumCreationDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, mininumCreationDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getOrders(subdomain: String, accessToken: String): Task[ShopifyPage[ShopifyOrder]]

    def getProduct(subdomain: String, accessToken: String, productId: String): Task[ShopifyProduct]

    def getProductCount(subdomain: String, accessToken: String): Task[Int]

    def getProductMetafields(subdomain: String, accessToken: String, productId: String): Task[List[Metafield]]

    def getProducts(subdomain: String, accessToken: String, pageInfo: String, pageSize: Int): Task[ShopifyPage[ShopifyProduct]]

    def getProducts(subdomain: String, accessToken: String, pageSize: Int): Task[ShopifyPage[ShopifyProduct]]

    def getProducts(subdomain: String, accessToken: String): Task[ShopifyProducts]

    def getRecurringApplicationCharge(subdomain: String, accessToken: String, chargeId: String): Task[ShopifyRecurringApplicationCharge]

    def getShop(subdomain: String, accessToken: String): Task[ShopifyShop]

    def getUpdatedOrdersCreatedBefore(subdomain: String, accessToken: String, minimumUpdatedAtDate: DateTime, maximumUpdatedAtDate: DateTime, maximumCreatedAtDate: DateTime, pageSize: Int): Task[ShopifyPage[ShopifyOrder]]

    def getVariant(subdomain: String, accessToken: String, variantId: String): Task[ShopifyVariant]

    def getVariantMetafields(subdomain: String, accessToken: String, variantId: String): Task[List[Metafield]]

    def refund(subdomain: String, accessToken: String, request: ShopifyRefundCreationRequest): Task[ShopifyRefund]

    def revokeOAuthToken(subdomain: String, accessToken: String): Task[Boolean]

    def searchCustomers(subdomain: String, accessToken: String, query: String): Task[ShopifyPage[ShopifyCustomer]]

    def updateCustomer(subdomain: String, accessToken: String, request: ShopifyCustomerUpdateRequest): Task[ShopifyCustomer]

    def updateFulfillment(subdomain: String, accessToken: String, request: ShopifyFulfillmentUpdateRequest): Task[ShopifyFulfillment]

    def updateInventoryLevel(subdomain: String, accessToken: String, inventoryItemId: String, locationId: String, quantity: Long): Task[ShopifyInventoryLevel]

    def updateOrderShippingAddress(subdomain: String, accessToken: String, request: ShopifyOrderShippingAddressUpdateRequest): Task[ShopifyOrder]

    def updateProduct(subdomain: String, accessToken: String, request: ShopifyProductUpdateRequest): Task[ShopifyProduct]

    def updateVariant(subdomain: String, accessToken: String, request: ShopifyVariantUpdateRequest): Task[ShopifyVariant]
}