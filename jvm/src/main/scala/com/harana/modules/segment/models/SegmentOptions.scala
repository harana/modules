package com.harana.modules.segment.models

import java.util.Date

case class SegmentOptions(context: Map[String, _] = Map(),
                          enabledIntegrations: Map[String, Boolean] = Map(),
                          integrationOptions: Option[(String, Map[String, _ <: Object])] = None,
                          isAnonymous: Boolean = false,
                          messageId: Option[String] = None,
                          timestamp: Option[Date] = None){}