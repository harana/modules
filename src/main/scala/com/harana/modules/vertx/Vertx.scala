package com.harana.modules.vertx

import com.harana.modules.vertx.models._
import com.harana.modules.vertx.proxy.WSURI
import io.vertx.core.eventbus.{EventBus, MessageConsumer}
import io.vertx.core.http.HttpServer
import io.vertx.core.net.{NetServer, NetServerOptions}
import io.vertx.core.shareddata.{AsyncMap, Counter, Lock}
import io.vertx.core.{Context, MultiMap, Vertx => VX}
import io.vertx.ext.web.RoutingContext
import io.vertx.servicediscovery.Record
import org.jose4j.jwk.JsonWebKeySet
import org.pac4j.core.profile.UserProfile
import zio.macros.accessible
import zio.{Task, UIO}

import java.net.URI

@accessible
trait Vertx {

  def subscribe(address: Address, `type`: String, onMessage: String => Task[Unit]): Task[MessageConsumer[String]]
  def unsubscribe(consumer: MessageConsumer[String]): Task[Unit]
  def publishMessage(address: Address, messageType: String, payload: String): Task[Unit]
  def publishMessage(address: Address, `type`: String): Task[Unit]
  def sendMessage(address: Address, `type`: String, message: String): Task[Unit]
  def sendMessage(address: Address, `type`: String): Task[Unit]

  def service(name: String): Task[Option[Record]]
  def services(filters: Map[String, String]): Task[List[Record]]
  def registerServiceListener(name: String, onChange: Record => Unit): UIO[Unit]
  def deregisterServiceListener(name: String): UIO[Unit]

  def lock(name: String): Task[Lock]
  def lockWithTimeout(name: String, timeoutSeconds: String, onLock: Lock => Task[Unit]): Task[Lock]

  def getCounter(name: String): Task[Counter]

  def clearMap[K, V](name: String): Task[Unit]
  def getMap[K, V](name: String): Task[AsyncMap[K, V]]
  def getMapKeys[K, V](name: String): Task[Set[K]]
  def getMapValues[K, V](name: String): Task[List[V]]
  def getMapValue[K, V](name: String, key: K): Task[Option[V]]
  def putMapValue[K, V](name: String, key: K, value: V, ttl: Option[Long] = None): Task[Unit]
  def removeMapValue[K, V](name: String, key: K): Task[Unit]
  def putMapValueIfAbsent[K, V](name: String, key: K, value: V, ttl: Option[Long] = None): Task[V]

  def getOrCreateContext: UIO[Context]
  def close: Task[Unit]

  def eventBus: UIO[EventBus]
  def startHttpServer(domain: String,
                      proxyDomain: Option[String] = None,
                      routes: List[Route] = List(),
                      clustered: Boolean = false,
                      defaultHandler: Option[RouteHandler] = None,
                      proxyMapping: Option[RoutingContext => Task[Option[URI]]] = None,
                      webSocketProxyMapping: Option[WebSocketHeaders => Task[WSURI]] = None,
                      errorHandlers: Map[Int, RoutingContext => Task[Response]] = Map(),
                      eventBusInbound: List[String] = List(),
                      eventBusOutbound: List[String] = List(),
                      authTypes: List[AuthType] = List(),
                      additionalAllowedHeaders: Set[String] = Set(),
                      postLogin: Option[(RoutingContext, Option[UserProfile]) => Task[Response]] = None,
                      sessionRegexp: Option[String] = None,
                      jwtKeySet: Option[JsonWebKeySet] = None,
                      logActivity: Boolean = false): Task[HttpServer]
  def startNetServer(listenHost: String, listenPort: Int, options: Option[NetServerOptions] = None): Task[NetServer]
  def startTcpEventBusServer(listenHost: String, listenPort: Int, inAddressRegex: String, outAddressRegex: String): Task[Unit]
  def underlying: UIO[VX]

}