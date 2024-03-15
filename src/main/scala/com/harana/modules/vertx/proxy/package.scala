package com.harana.designer.backend.modules

import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.multipart.MultipartForm

import java.net.URLDecoder
import java.util.Formatter
import scala.collection.BitSet

package object proxy {

  val hopByHopHeaders: Map[String, Boolean] =
    Map(
      "Connection" -> true,
      "Keep-Alive" -> true,
      "Proxy-Authenticate" -> true,
      "Proxy-Authorization" -> true,
      "TE" -> true,
      "Trailers" -> true,
      "Transfer-Encoding" -> true,
      "Upgrade" -> true
    )


  val asciiQueryCharacters: BitSet = {
    var bitset = scala.collection.mutable.BitSet(128)
    List(
      "abcdefghijklmnopqrstuvwxyz",
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
      "0123456789",
      "_-!.~'()*",
      ",;:$&+=",
      "?/[]@","%").foreach(l => l.foreach(bitset += _.toInt))
    bitset
  }

  def rewriteLocation(from: String, to: String) = {
    val fromParts = urlParts(from)
    val toParts = urlParts(to, true)

     (fromParts, toParts) match {
      case ((_, Some(fromPath)), (Some(toHost), Some(toPath))) => s"$toHost/$toPath/$fromPath"
      case ((_, Some(fromPath)), (Some(toHost), None)) => s"$toHost/$fromPath"
      case ((_, None), (Some(toHost), Some(toPath))) => s"$toHost/$toPath"
      case ((_, None), (Some(toHost), None)) => toHost
      case ((Some(fromHost), None), (None, None)) => fromHost
      case ((None, Some(fromPath)), (None, Some(toPath))) => s"$toPath/$fromPath"
      case ((None, Some(fromPath)), (None, None)) => s"/$fromPath"      
      case _ => ""
    }
  }

  def urlParts(uri: String, ignorePath: Boolean = false): (Option[String], Option[String]) = {
    val cleanUri = clean(uri)
    val delimiter = cleanUri.indexOf("/", 8)
    if (cleanUri.startsWith("http://"))
      if (delimiter > 0) (Some(clean(cleanUri.substring(0, delimiter))), if (ignorePath) None else Some(clean(cleanUri.substring(delimiter, cleanUri.length()))))
      else (Some(cleanUri), None)
    else
      (None, if (cleanUri.isEmpty || ignorePath) None else Some(cleanUri))
  }

  private def clean(uri: String) = {
    var cleanUri = uri.trim()
    if (cleanUri.startsWith("/")) cleanUri = cleanUri.drop(1)
    if (cleanUri.endsWith("/") && !cleanUri.contains("?")) cleanUri = cleanUri.dropRight(1)
    URLDecoder.decode(cleanUri, "UTF-8" );
  }

  def encodeUriQuery(in: CharSequence, encodePercent: Boolean): CharSequence = {
    var outBuf: Option[StringBuffer] = None
    var formatter: Option[Formatter] = None

    for (i <- 0 until in.length) {
      val c = in.charAt(i)
      var escape = true

      if (c < 128) {
        if (asciiQueryCharacters(c.toInt) && !(encodePercent && c == '%')) escape = false
      } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) escape = false

      if (!escape) {
        if (outBuf.nonEmpty) outBuf.get.append(c)
      }else {
        if (outBuf.isEmpty) {
          outBuf = Some(new StringBuffer(in.length() + 5 * 3))
          outBuf.get.append(in, 0, i)
          formatter = Some(new Formatter(outBuf.get))
        }else{
          formatter.get.format("%%%02X", Integer.valueOf(c.toInt))
        }
      }
    }
    if (outBuf.nonEmpty) outBuf.get else in
  }


  def setXForwardedForHeader(routingContext: RoutingContext, proxyRequest: HttpRequest[Buffer], scheme: String): Unit = {
    val forHeaderName = "X-Forwarded-For"
    var forHeader = routingContext.request.remoteAddress.host
    val existingForHeader = routingContext.request.headers.get(forHeaderName)
    if (existingForHeader != null) forHeader = existingForHeader + ", " + forHeader
    proxyRequest.headers.set(forHeaderName, forHeader)
    proxyRequest.headers.set("X-Forwarded-Proto", scheme)
  }


  def getContentLength(routingContext: RoutingContext): Long = {
    val contentLengthHeader = routingContext.request.headers.get(HttpHeaders.CONTENT_LENGTH)
    if (contentLengthHeader != null) contentLengthHeader.toLong else -1L
  }


  def multipartForm(routingContext: RoutingContext) = {
    val value = routingContext.request.getHeader(HttpHeaders.CONTENT_TYPE)
    if (value != null) {
      value.contains(HttpHeaderValues.MULTIPART_FORM_DATA.toString)
    }else{
      false
    }
  }


  def createMultipartForm(routingContext: RoutingContext, uploadsDirectory: String) = {
    val result = MultipartForm.create
    val formAttributes = routingContext.request.formAttributes
    val formAttributesIterator = formAttributes.iterator

    while (formAttributesIterator.hasNext) {
      val entry = formAttributesIterator.next
      result.attribute(entry.getKey, entry.getValue)
    }

    val uploadsIterator = routingContext.fileUploads.iterator

    while (uploadsIterator.hasNext) {
      val uploadFile = uploadsIterator.next
      val fileName = uploadFile.uploadedFileName.replace(uploadsDirectory + "\\", "").replace(uploadsDirectory + "/", "")
      result.binaryFileUpload(uploadFile.name, uploadFile.fileName, uploadsDirectory + "/" + fileName, uploadFile.contentType)
    }
    result
  }
}