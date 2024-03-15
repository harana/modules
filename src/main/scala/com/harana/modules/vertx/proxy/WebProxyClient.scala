package com.harana.modules.vertx.proxy

import com.harana.designer.backend.modules.proxy._
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{Cookie, HttpHeaders}
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.{HttpRequest, HttpResponse, WebClient}
import org.apache.commons.lang3.mutable.MutableBoolean

import java.net.{HttpCookie, URI}
import java.util.Map.Entry
import java.util.function.Function
import scala.jdk.CollectionConverters._


class WebProxyClient(client: WebClient, clientOptions: WebProxyClientOptions) {

  var serverRequestUriInfo: URIInfo = _
  var contentFilterEnabled = new MutableBoolean(false)
  var uploadsDirectory: String = "file-uploads"

  var cookieFilterRequest: Function[String, Boolean] = _
  var cookieFilterResponse: Function[HttpCookie, Boolean] = _
  var headerFilterResponse: Function[Entry[String, String], Boolean] = _
  var contentFilter: Function[Buffer, Buffer] =  _


  def execute(rc: RoutingContext, urlPattern: String, targetUri: URI): Unit = {
    var domain = urlPattern.replace("/*", "")
    if (domain.isEmpty) domain = "/"
    serverRequestUriInfo = URIInfo(new URI(rc.request.absoluteURI), domain)

    val handler = requestHandler(rc, targetUri)
    val method = rc.request.method
    val proxyRequestUri = rewriteUrlFromRequest(targetUri, serverRequestUriInfo.queryString, serverRequestUriInfo.pathInfo)

    val proxyRequest = client.requestAbs(method, proxyRequestUri).ssl(targetUri.getScheme.equalsIgnoreCase("https"))

    val multipart = multipartForm(rc)
    if (multipart) copyRequestHeadersForMultipartForm(rc, proxyRequest, targetUri)
    else copyRequestHeaders(rc, proxyRequest, targetUri)
    if (clientOptions.forwardIP) setXForwardedForHeader(rc, proxyRequest, serverRequestUriInfo.scheme)

    if (multipart) proxyRequest.sendMultipartForm(createMultipartForm(rc, uploadsDirectory), handler)
    else {
      val buffer = rc.getBody
      if (buffer != null) {
        val copy = buffer.copy().asInstanceOf[Buffer]
        proxyRequest.headers.set(HttpHeaders.CONTENT_LENGTH, buffer.length.toString)
        proxyRequest.sendBuffer(copy, handler)
      }
      else proxyRequest.send(handler)
    }
  }
  

  private def requestHandler(rc: RoutingContext, targetUri: URI): Handler[AsyncResult[HttpResponse[Buffer]]] = asyncResult => {
    try if (asyncResult.succeeded) {
      val statusCode = asyncResult.result.statusCode()
      rc.response.setStatusCode(statusCode)
      rc.response.setStatusMessage(asyncResult.result.statusMessage())
      copyResponseHeaders(asyncResult.result, rc, targetUri, headerFilterResponse)

      if (statusCode == HttpResponseStatus.NOT_MODIFIED.code) {
        rc.response.headers.set(HttpHeaders.CONTENT_LENGTH, "0")
      }
      else {
        val proxyResponse = asyncResult.result
        if (proxyResponse.body() != null)
          if (!rc.response.closed && !rc.response.ended && rc.response.bytesWritten == 0) {
            val buffer = proxyResponse.body().copy.asInstanceOf[Buffer]
            //if (contentFilterEnabled.isTrue) buffer = contentFilter.apply(buffer)
            rc.response.headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length))
            rc.response.write(buffer)
          }
      }

      if (!rc.response.closed && !rc.response.ended) rc.response.end()
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        rc.fail(e)
    }
  }
  

  def rewriteUrlFromRequest(targetUri: URI, queryString: String, pathInfo: String) = {
    val uri = new StringBuilder(500)
    uri.append(targetUri)

    if (pathInfo != null) uri.append(encodeUriQuery(pathInfo, encodePercent = true))
    var fragment: String = null
    var query = queryString

    if (query != null) {
      val fragIdx = query.indexOf('#')
      if (fragIdx >= 0) {
        fragment = queryString.substring(fragIdx + 1)
        query = query.substring(0, fragIdx)
      }
    }

    if (query != null && query.nonEmpty) {
      uri.append('?')
      uri.append(encodeUriQuery(query, encodePercent = false))
    }
    if (clientOptions.sendUrlFragment && fragment != null) {
      uri.append('#')
      uri.append(encodeUriQuery(fragment, encodePercent = false))
    }

    uri.toString
  }


  def copyRequestHeaders(rc: RoutingContext, proxyRequest: HttpRequest[Buffer], targetObj: URI): Unit = {
    val headers = rc.request.headers.iterator
    while (headers.hasNext) {
      val header = headers.next
      val headerName = header.getKey
      var headerValue = header.getValue

      if (!headerName.equalsIgnoreCase("Content-Length") && !hopByHopHeaders.contains(headerName)) {
        if (!clientOptions.preserveHost && headerName.equalsIgnoreCase("Host")) {
          headerValue = targetObj.getHost
          if (targetObj.getPort != -1) headerValue += ":" + targetObj.getPort
        }
        else if (header.getKey.equalsIgnoreCase("Cookie")) headerValue = getRealCookie(headerValue, cookieFilterRequest)
        proxyRequest.headers.set(headerName, headerValue)
      }
    }
  }


  def copyRequestHeadersForMultipartForm(rc: RoutingContext, proxyRequest: HttpRequest[Buffer], targetObj: URI): Unit = {
    val headers = rc.request.headers.iterator

    while (headers.hasNext) {
      val header = headers.next
      val headerName = header.getKey
      var headerValue = header.getValue

      if (!clientOptions.preserveHost && headerName.equalsIgnoreCase("Host")) {
        headerValue = targetObj.getHost
        if (targetObj.getPort != -1) headerValue += ":" + targetObj.getPort
        proxyRequest.headers.set(headerName, headerValue)
      }
      else if (header.getKey.equalsIgnoreCase("Cookie")) {
        headerValue = getRealCookie(headerValue, cookieFilterRequest)
        proxyRequest.headers.set(headerName, headerValue)
      }
      else if (header.getKey.equalsIgnoreCase("Authorization")) {
        proxyRequest.headers.set(headerName, headerValue)
      }
    }
  }


  def copyResponseHeaders(proxyResponse: HttpResponse[Buffer], rc: RoutingContext, targetUri: URI, filter: Function[Entry[String, String], Boolean]): Unit = {

    val headers = proxyResponse.headers()
      .set("Content-Security-Policy", s"frame-ancestors ${clientOptions.iFrameAncestors.map(a => s"*.$a").mkString(" ")} 'self';")
    val iterator = headers.iterator

    while (iterator.hasNext) {
      val header = iterator.next
      //if (filter != null) if (!filter.apply(header)) copyResponseHeader(proxyResponse, rc, targetUri, header)
      //else
      copyResponseHeader(proxyResponse, rc, targetUri, header)
    }
  }


  def copyResponseHeader(proxyResponse: HttpResponse[Buffer], rc: RoutingContext, targetUri: URI, header: Entry[String, String]): Unit = {
    val headerName = header.getKey
    if (hopByHopHeaders.contains(headerName)) return

    val headerValue = header.getValue
    if (headerName.equalsIgnoreCase("Set-Cookie") || headerName.equalsIgnoreCase("Set-Cookie2")) {
      copyProxyCookie(rc, headerValue)
    }else if (headerName.equalsIgnoreCase("Location"))
      rc.response.headers.add(headerName, rewriteLocation(headerValue, serverRequestUriInfo.requestUrl))
    else rc.response.headers.add(headerName, headerValue)
  }


  def copyProxyCookie(rc: RoutingContext, headerValue: String): Unit = {
    var path = ""
    if (!clientOptions.preserveCookiesContextPath) path = serverRequestUriInfo.contextPath
    if (!clientOptions.preserveCookiesProxyPath) path += serverRequestUriInfo.proxyPath
    if (path.isEmpty) path = "/"

    for (cookie <- HttpCookie.parse(headerValue).asScala) {
      //if (!cookieFilterResponse.apply(cookie)) {
        val serverCookie = Cookie.cookie(cookie.getName, cookie.getValue)
        val maxAge = cookie.getMaxAge().toInt
        serverCookie.setMaxAge(Int.MaxValue)
        serverCookie.setPath(path)
        serverCookie.setSecure(cookie.getSecure)
        rc.response.addCookie(serverCookie)
      //}
    }
  }


  def getRealCookie(cookieValue: String, filter: Function[String, Boolean]) = {
    val escapedCookie = new StringBuilder
    val cookies = cookieValue.split("[;,]")
    for (cookie <- cookies) {
      val cookieSplit = cookie.split("=")
      if (cookieSplit.length == 2) {
        var cookieName = cookieSplit(0).trim
        //if (!filter.apply(cookieName)) {
            if (escapedCookie.nonEmpty) escapedCookie.append("; ")
            escapedCookie.append(cookieName).append("=").append(cookieSplit(1).trim)
        //}
      }
    }
    escapedCookie.toString
  }


  def rewriteUrlFromResponse(targetUri: URI, theUrl: String) = {
    if (theUrl.startsWith(targetUri.toString)) {
      val curUrl = new StringBuffer(serverRequestUriInfo.requestUrl)
      var pos = curUrl.indexOf("://")
      if (pos >= 0) {
        pos = curUrl.indexOf("/", pos + 3)
        if (pos >= 0) curUrl.setLength(pos)
      }
      curUrl.append(serverRequestUriInfo.contextPath)
      curUrl.append(serverRequestUriInfo.proxyPath)
      curUrl.append(theUrl, targetUri.toString.length, theUrl.length)
      curUrl.toString
    } else {
      theUrl
    }
  }
}