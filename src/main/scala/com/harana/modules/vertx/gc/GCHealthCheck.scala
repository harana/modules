package com.harana.modules.vertx.gc

import java.lang.management.ManagementFactory
import scala.jdk.CollectionConverters._

object GCHealthCheck {

  private val PERCENT_OF_PERCENT_FACTOR = 10000
  private val PERCENT_FACTOR = 100.0

  private var lastTimeAccessed = 0L
  private var lastCollectionTime = 0L

  private val maxPercent = 20

  /**
   * The HealthCheck does not make sense when calling healthCheck.current() within a very short interval.
   * In that case, it will most likely be in failed state (100% GC).
   * The checks should be executed with some delay, to get reasonable percentage values.
   *
   * @return The current garbage collection details
   */
  def current: GCDetails = {
    val collectionTime = getCollectionTimeMillis
    val accessTimeMillis = System.currentTimeMillis
    updateTimeStamps(collectionTime, accessTimeMillis)

    GCDetails(maxPercent, getGCTimeInPercent(collectionTime), accessTimeMillis)
  }

  private def getGCTimeInPercent(collectionTime: Long): Double = {
    if (lastTimeAccessed == 0) return 0
    val timeSinceLastAccessed = System.currentTimeMillis - lastTimeAccessed
    if (timeSinceLastAccessed <= 0) return 0
    val thisCollectionTime = collectionTime - lastCollectionTime
    val gcTimeInPercentOfPercents = thisCollectionTime * PERCENT_OF_PERCENT_FACTOR / timeSinceLastAccessed
    gcTimeInPercentOfPercents / PERCENT_FACTOR
  }

  private def updateTimeStamps(collectionTimeMillis: Long, accesTimeMillis: Long): Unit = {
    lastCollectionTime = collectionTimeMillis
    lastTimeAccessed = accesTimeMillis
  }

  private def getCollectionTimeMillis =
    ManagementFactory.getGarbageCollectorMXBeans.asScala
      .map(_.getCollectionTime)
      .filter(_ != -1)
      .sum
}
