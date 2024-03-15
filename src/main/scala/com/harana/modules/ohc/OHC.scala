package com.harana.modules.ohc

import org.caffinitas.ohc.{CacheLoader, OHCache}
import zio.UIO
import zio.macros.accessible

import java.nio.ByteBuffer

@accessible
trait OHC {
    def newCache[K, V](hashTableSize: Option[Int] = None,
                       chunkSize: Option[Int] = None,
                       capacity: Option[Long] = None,
                       segmentCount: Option[Int] = None): UIO[OHCache[K, V]]

    def put[K, V](cache: OHCache[K, V], key: K, value: V, expireAt: Option[Long] = None): UIO[Boolean]

    def putIfAbsent[K, V](cache: OHCache[K, V], key: K, value: V, expireAt: Option[Long] = None): UIO[Boolean]

    def putAll[K, V](cache: OHCache[K, V], values: Map[K, V]): UIO[Unit]

    def addOrReplace[K, V](cache: OHCache[K, V], key: K, oldValue: V, newValue: V, expireAt: Option[Long] = None): UIO[Boolean]

    def remove[K, V](cache: OHCache[K, V], key: K): UIO[Boolean]

    def removeAll[K, V](cache: OHCache[K, V], keys: Set[K]): UIO[Unit]

    def clear[K, V](cache: OHCache[K, V]): UIO[Unit]

    def get[K, V](cache: OHCache[K, V], key: K): UIO[V]

//    def getAsBytes[K, V](cache: OHCache[K, V], key: K, updateLRU: Boolean = false): UIO[ByteBuffer]

    def getWithLoader[K, V](cache: OHCache[K, V], key: K, loader: CacheLoader[K, V], expiresAt: Option[Long] = None): UIO[V]

    def containsKey[K, V](cache: OHCache[K, V], key: K): UIO[Boolean]

    def size[K, V](cache: OHCache[K, V]): UIO[Long]
}