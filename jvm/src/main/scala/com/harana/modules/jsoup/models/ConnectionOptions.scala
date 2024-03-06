package com.harana.modules.jsoup.models

import org.jsoup.Connection

import javax.net.ssl.SSLSocketFactory

case class ConnectionOptions(cookies: Map[String, String] = Map(),
                             data: Map[String, String] = Map(),
                             followRedirects: Option[Boolean] = None,
                             headers: Map[String, String] = Map(),
                             ignoreContentType: Option[Boolean] = None,
                             ignoreHttpErrors: Option[Boolean] = None,
                             maxBodySize: Option[Int] = None,
                             method: Option[Connection.Method] = None,
                             postDataCharset: Option[String] = None,
                             proxy: Option[Proxy] = None,
                             referrer: Option[String] = None,
                             requestBody: Option[String] = None,
                             sslSocketFactory: Option[SSLSocketFactory] = None,
                             timeout: Option[Int] = None)