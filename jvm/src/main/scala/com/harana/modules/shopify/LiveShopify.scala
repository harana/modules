package com.harana.modules.shopify

import com.harana.modules.core.config.Config
import com.harana.modules.core.http.Http
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.shopify.models._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import purecsv.safe._
import zio.{Ref, Task, ZIO, ZLayer}

import java.io.File
import java.time.ZoneId
import java.time.temporal.{ChronoUnit, TemporalAdjusters}
import scala.util.Try

object LiveShopify {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      http          <- ZIO.service[Http]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveShopify(config, http, logger, micrometer)
  }
}

case class LiveShopify(config: Config, http: Http, logger: Logger, micrometer: Micrometer) extends Shopify {

  def forecastInventory(implicit connection: ShopifyConnection): Task[List[Output]] =
    for {
      products                    <- products(limit = Some(250), status = Some("active")).map(_.items)
      _                           <- logger.info(s"Number of products: ${products.size}")
      productMap                  = products.map(p => p.id -> p).toMap
      variants                    = products.flatMap(p => p.variants)
      variantsMap                 = variants.map(v => v.id -> v).toMap
      _                           <- logger.info(s"Number of variants: ${variantsMap.size}")

      outputs                     = variants.map(v => Output(productMap(v.productId).title, v.title, v.sku.getOrElse(""), v.id, v.option1.getOrElse(""), v.option2.getOrElse(""), v.option3.getOrElse(""), "0", "0", "0", "0", "0"))
      orders                      <- all(orders(limit = Some(250), status = Some("any")))
      _                           <- logger.info(s"Number of orders: ${orders.size}")

      ordersByDate                = orders.groupBy(o => o.createdAt.atZone(ZoneId.systemDefault()).`with`(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS))
      lineItemsByDate             = ordersByDate.view.mapValues(orders => orders.flatMap(_.lineItems.map(li => (lineItemTitle(li), li.quantity))))
      lineItemsByVariantIdMap     = orders.flatMap(_.lineItems.map(li => li.variantId -> li)).toMap
      groupedLineItemsByDate      = lineItemsByDate.mapValues(lineItems => lineItems.groupBy(_._1).view.mapValues(_.map(_._2).sum).toList.sortBy(_._1))

      groupedLineItemsByDateMap   = groupedLineItemsByDate.mapValues(sumByKeys).mapValues(_.toMap)
      sortedDates                 = groupedLineItemsByDate.keys.toList.sortBy(_.toString).take(3)
      _                           <- logger.info(s"Dates to output: ${sortedDates.map(_.toString)}")

      middleOutputs = outputs.map { o =>
        if (lineItemsByVariantIdMap.contains(o.variantId)) {
          val lineItem = lineItemsByVariantIdMap(o.variantId)

          val month1 = Try(groupedLineItemsByDateMap(sortedDates.head)(lineItemTitle(lineItem))).toOption.getOrElse(0L)
          val month2 = Try(groupedLineItemsByDateMap(sortedDates(1))(lineItemTitle(lineItem))).toOption.getOrElse(0L)
          val month3 = Try(groupedLineItemsByDateMap(sortedDates(2))(lineItemTitle(lineItem))).toOption.getOrElse(0L)
          val total = month1 + month2 + month3
          o.copy(month1Sales = month1.toString, month2Sales = month2.toString, month3Sales = month3.toString, totalSales = total.toString)
        } else {
          o.copy(month1Sales = "-", month2Sales = "-", month3Sales = "-", totalSales = "-")
        }
      }

      location <- locations(connection).map(_.items.head)
      _ <- logger.info(s"Found location with id: ${location.id}")

      inventoryLevels <- all(inventoryLevels(limit = Some(250), locationIds = List(location.id)))
      inventoryLevelsMap = inventoryLevels.map(il => il.inventoryItemId -> il.available).toMap
      finalOutputs = middleOutputs.map(o => o.copy(inventoryLevel = Try(inventoryLevelsMap(variantsMap(o.variantId).inventoryItemId).toString).toOption.getOrElse("-")))

      _ = finalOutputs.writeCSVToFile(new File("/tmp/output.csv"))

    } yield finalOutputs


  private def sumByKeys[A](tuples: List[(A, Long)]) : List[(A, Long)] = {
    tuples.groupBy(_._1).view.mapValues(_.map(_._2).sum).toList
  }


  private def lineItemTitle(li: LineItem) =
    s"${li.productId}-${li.variantId}"


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
             fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[Order]] =
    getList[Order](s"orders", Map(
      "ids" -> ids.mkString(","),
      "limit" -> limit.getOrElse(50).toString,
      "status" -> status.getOrElse("")
    ))


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
               fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[models.Product]] =
    getList[models.Product](s"products", Map(
      "ids" -> ids.mkString(","),
      "limit" -> limit.getOrElse(50).toString,
      "status" -> status.getOrElse(""))
    )


  def inventoryLevels(inventoryItemIds: List[Long] = List(),
                      locationIds: List[Long] = List(),
                      limit: Option[Int] = None,
                      updatedAtMin: Option[String] = None)
                     (implicit connection: ShopifyConnection): Task[Page[InventoryLevel]] =
    getList[InventoryLevel](s"inventory_levels", Map(
      "limit" -> limit.getOrElse(50).toString,
      "location_ids" -> locationIds.map(_.toString).mkString(",")))


  def locations(implicit connection: ShopifyConnection): Task[Page[Location]] =
    getList[Location](s"locations", Map())


  def customer(id: String,
               fields: List[String] = List())
              (implicit connection: ShopifyConnection): Task[Customer] =
    get[Customer](s"customers/$id", Map())


  def createCustomerMetafield(customerId: String, metafield: Metafield)(implicit connection: ShopifyConnection): Task[Metafield] =
    post[Metafield](s"customers/$customerId/metafields", Map(), metafield)


  def customerMetafield(customerId: String, metafieldId: String)(implicit connection: ShopifyConnection): Task[Metafield] =
    get[Metafield](s"customers/$customerId/metafields/$metafieldId.json", Map())


  def setCustomerMetafield(customerId: String, metafieldId: String, metafieldType: String, metafieldValue: String)(implicit connection: ShopifyConnection): Task[Unit] =
    post[String](s"customers/$customerId/metafields/$metafieldId.json", Map(),
      Map("metafield" -> Map("id" -> metafieldId, "value" -> metafieldValue, "type" -> metafieldType)).asJson.noSpaces
    ).ignore

  def orderMetafield(orderId: String, metafieldId: String)(implicit connection: ShopifyConnection): Task[Metafield] =
    get[Metafield](s"orders/$orderId/metafields/$metafieldId.json", Map())


  def setOrderMetafield(orderId: String, metafieldId: String, metafieldType: String, metafieldValue: String)(implicit connection: ShopifyConnection): Task[Unit] =
    post[String](s"orders/$orderId/metafields/$metafieldId.json", Map(),
      Map("metafield" -> Map("id" -> metafieldId, "value" -> metafieldValue, "type" -> metafieldType)).asJson.noSpaces
    ).ignore


  def product(id: String,
              fields: List[String] = List())(implicit connection: ShopifyConnection): Task[models.Product] =
    get[models.Product](s"products/$id", Map())


  def productVariants(productId: Long,
                      limit: Option[Int] = None,
                      presentmentCurrencies: List[String] = List(),
                      sinceId: Option[String] = None,
                      fields: List[String] = List())(implicit connection: ShopifyConnection): Task[Page[ProductVariant]] =
    getList[ProductVariant](s"products/$productId/variants", Map(
      "fields" -> fields.mkString(","),
      "limit" -> limit.getOrElse(50).toString,
      "presentment_currencies" -> presentmentCurrencies.mkString(","),
      "since_id" -> sinceId.getOrElse("")))


  def uploadImage(sourceUrl: String)(implicit connection: ShopifyConnection): Task[Unit] =
    for {
      url <- ZIO.succeed(s"https://${connection.subdomain}.myshopify.com/admin/api/2023-07/graphql.json")
      _   <- http.post(
        url,
        credentials = Some((connection.apiKey, connection.password)),
        body = Some(
          s"""{
            "query": "mutation fileCreate($$files: [FileCreateInput!]!) { fileCreate(files: $$files) { files { alt createdAt } } }",
            "variables": {
              "files": {
                "contentType": "IMAGE",
                "originalSource": "$sourceUrl"
              }
            }
          }"""
        )
      ).mapBoth(e => new Exception(e.toString), _.body().string())
    } yield ()


  def previousPage[T](page: Page[T])(implicit connection: ShopifyConnection, d: Decoder[T]): Task[Option[Page[T]]] =
    ZIO.foreach(page.previousUrl)(url => getUrlList[T](url, Map()))


  def nextPage[T](page: Page[T])(implicit connection: ShopifyConnection, d: Decoder[T]): Task[Option[Page[T]]] =
    ZIO.foreach(page.nextUrl)(url => getUrlList[T](url, Map()))


  def all[T](fn: => Task[Page[T]])(implicit connection: ShopifyConnection, d : Decoder[T]): Task[List[T]] =
    for {
      itemsRef            <- Ref.make[List[T]](List())
      initialPage         <- fn
      currentPageRef      <- Ref.make[Option[Page[T]]](Some(initialPage))
      _                   <- (for {
                                currentPage       <- currentPageRef.get
                                existingItems     <- itemsRef.get
                                _                 <- itemsRef.set (existingItems ++ currentPage.get.items)
                                nextPage          <- ZIO.foreach(currentPage)(nextPage[T](_)).map(_.flatten)
                                _                 <- currentPageRef.set(nextPage)
                              } yield ()).repeatWhileZIO { _ => currentPageRef.get.map(_.nonEmpty) }
      items               <- itemsRef.get
    } yield items


  private def get[T](endpoint: String, parameters: Map[String, String])
                    (implicit connection: ShopifyConnection, d: Decoder[T]): Task[T] =
    for {
      url           <- ZIO.succeed(s"https://${connection.subdomain}.myshopify.com/admin/api/2023-04/$endpoint.json")
      response      <- http.get(url, params = parameters.map{ case (k, v) => k -> List(v) }, credentials = Some((connection.apiKey, connection.password))).mapBoth(e => new Exception(e.toString), _.body().string())
      obj           <- ZIO.fromEither(decode[T](response))
    } yield obj


  private def getList[T](endpoint: String, parameters: Map[String, String])
                        (implicit connection: ShopifyConnection, d: Decoder[T]): Task[Page[T]] =
    for {
      url           <- ZIO.succeed(s"https://${connection.subdomain}.myshopify.com/admin/api/2023-04/$endpoint.json")
      page          <- getUrlList[T](url, parameters)
    } yield page


  private def getUrlList[T](url: String, parameters: Map[String, String])
                           (implicit connection: ShopifyConnection, d: Decoder[T]): Task[Page[T]] =
    for {
      response      <- http.get(url, params = parameters.map{ case (k, v) => k -> List(v) }, credentials = Some((connection.apiKey, connection.password))).mapError(e => new Exception(e.toString))

      rel           =  Option(response.header("link"))
      relUrl        =  rel.map(r => r.substring(1, r.indexOf(">")))
      relType       =  rel.map(r => r.substring(r.indexOf("rel=")+5, r.length-1))

      cursor        <- ZIO.attempt(parse(response.body().string).toOption.get.hcursor)
      root          <- ZIO.attempt(cursor.keys.get.head)
      json          <- ZIO.attempt(cursor.downField(root).focus.get)
      items         <- ZIO.fromEither(json.as[List[T]]).onError(e => logger.error(e.prettyPrint))

      page          =  (rel, relType) match {
                          case (None, _)                                    => Page(None, None, items)
                          case (Some(_), Some(rt)) if (rt == "previous")    => Page(relUrl, None, items)
                          case (Some(_), Some(rt)) if (rt == "next")        => Page(None, relUrl, items)
                        }
    } yield page


  private def post[T](endpoint: String, parameters: Map[String, String], body: T)
                     (implicit connection: ShopifyConnection, d: Decoder[T], e: Encoder[T]): Task[T] =
    for {
      url       <- ZIO.succeed(s"https://${connection.subdomain}.myshopify.com/admin/api/2023-04/$endpoint.json")
      response  <- http.post(
                      url,
                      params = parameters.map { case (k, v) => k -> List(v) },
                      credentials = Some((connection.apiKey, connection.password)),
                      body = Some(body.asJson.noSpaces)
                   ).mapBoth(e => new Exception(e.toString), _.body().string())
      obj       <- ZIO.fromEither(decode[T](response))
    } yield obj


  private def put[T](endpoint: String, parameters: Map[String, String], body: T)
                    (implicit connection: ShopifyConnection, d: Decoder[T], e: Encoder[T]): Task[T] =
    for {
      url <- ZIO.succeed(s"https://${connection.subdomain}.myshopify.com/admin/api/2023-04/$endpoint.json")
      response <- http.put(
        url,
        params = parameters.map { case (k, v) => k -> List(v) },
        credentials = Some((connection.apiKey, connection.password)),
        body = Some(body.asJson.noSpaces)
      ).mapBoth(e => new Exception(e.toString), _.body().string())
      obj <- ZIO.fromEither(decode[T](response))
    } yield obj
}
