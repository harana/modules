package com.harana.modules.ohc

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import org.caffinitas.ohc.{CacheLoader, DirectValueAccess, OHCache, OHCacheBuilder}
import zio.{ZIO, ZLayer}

import java.nio.ByteBuffer
import scala.jdk.CollectionConverters._

object LiveOHC {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveOHC(config, logger, micrometer)
  }
}

case class LiveOHC(config: Config, logger: Logger, micrometer: Micrometer) extends OHC {

  def newCache[K, V](hashTableSize: Option[Int] = None,
                     chunkSize: Option[Int] = None,
                     capacity: Option[Long] = None,
                     segmentCount: Option[Int] = None) = {
    val builder = OHCacheBuilder.newBuilder[K, V]()
    if (hashTableSize.nonEmpty) builder.hashTableSize(hashTableSize.get)
    if (chunkSize.nonEmpty) builder.chunkSize(chunkSize.get)
    if (capacity.nonEmpty) builder.capacity(capacity.get)
    if (segmentCount.nonEmpty) builder.segmentCount(segmentCount.get)
    ZIO.succeed(builder.build())
  }

  def put[K, V](cache: OHCache[K, V], key: K, value: V, expireAt: Option[Long] = None) =
    ZIO.succeed(if (expireAt.nonEmpty) cache.put(key, value, expireAt.get) else cache.put(key, value))


  def putIfAbsent[K, V](cache: OHCache[K, V], key: K, value: V, expireAt: Option[Long] = None) =
    ZIO.succeed(if (expireAt.nonEmpty) cache.put(key, value, expireAt.get) else cache.putIfAbsent(key, value))


  def putAll[K, V](cache: OHCache[K, V], values: Map[K, V]) =
    ZIO.succeed(cache.putAll(values.asJava))


  def addOrReplace[K, V](cache: OHCache[K, V], key: K, oldValue: V, newValue: V, expireAt: Option[Long] = None) =
    ZIO.succeed(if (expireAt.nonEmpty) cache.addOrReplace(key, oldValue, newValue, expireAt.get) else cache.addOrReplace(key, oldValue, newValue))


  def remove[K, V](cache: OHCache[K, V], key: K) =
    ZIO.succeed(cache.remove(key))


  def removeAll[K, V](cache: OHCache[K, V], keys: Set[K]) =
    ZIO.succeed(cache.removeAll(keys.asJava))


  def clear[K, V](cache: OHCache[K, V]) =
    ZIO.succeed(cache.clear())


  def get[K, V](cache: OHCache[K, V], key: K) =
    ZIO.succeed(cache.get(key))


// FIXME
//  def getAsBytes[K, V](cache: OHCache[K, V], key: K, updateLRU: Boolean = false) =
//    ZIO.acquireReleaseWith[OHCache[K, V], DirectValueAccess, ByteBuffer](ZIO.succeed(cache.getDirect(key, updateLRU)), d => ZIO.succeed(d.close()), d => ZIO.succeed(d.buffer()))


  def getWithLoader[K, V](cache: OHCache[K, V], key: K, loader: CacheLoader[K, V], expireAt: Option[Long] = None) =
    ZIO.fromFutureJava(
      if (expireAt.nonEmpty) cache.getWithLoaderAsync(key, loader, expireAt.get) else cache.getWithLoaderAsync(key, loader)
    ).orDie


  def containsKey[K, V](cache: OHCache[K, V], key: K) =
    ZIO.succeed(cache.containsKey(key))


  def size[K, V](cache: OHCache[K, V]) =
    ZIO.succeed(cache.size())
}