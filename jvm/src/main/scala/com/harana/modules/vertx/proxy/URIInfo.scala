package com.harana.modules.vertx.proxy

import java.net.URI

case class URIInfo(uri: URI, domain: String) {

  def contextPath = domain
  def proxyPath = pathInfo + (if (uri.getQuery != null) "?" else "")
  def requestUrl = uri.toString
  def requestUri = uri.getPath + (if (uri.getQuery != null) "?" else "") + uri.getQuery
  def queryString = uri.getQuery
  def scheme = uri.getScheme
  def serverName = uri.getHost
  def serverPort = uri.getPort

  def pathInfo = {
    var result = uri.getPath.replaceFirst(domain, "")
    if (!result.startsWith("/")) result = "/" + result
    result
  }
}