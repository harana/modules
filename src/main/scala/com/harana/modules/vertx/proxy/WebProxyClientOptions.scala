package com.harana.modules.vertx.proxy

case class WebProxyClientOptions(log: Boolean = false,
                                 sendUrlFragment: Boolean = true,
                                 preserveHost: Boolean = false,
                                 preserveCookies: Boolean = true,
                                 forwardIP: Boolean = true,
                                 iFrameAncestors: List[String] = List(),
                                 preserveCookiesContextPath: Boolean = true,
                                 preserveCookiesProxyPath: Boolean = true,
                                 ssl: Boolean = false)
