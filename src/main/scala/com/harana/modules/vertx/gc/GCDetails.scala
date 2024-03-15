package com.harana.modules.vertx.gc

case class GCDetails(maxPercent: Int = 0,
                     gcTimeInPercent: Double = 0.0,
                     accessTimeMillis: Long = 0L) {

  def isHealthy: Boolean =
    gcTimeInPercent <= maxPercent
}