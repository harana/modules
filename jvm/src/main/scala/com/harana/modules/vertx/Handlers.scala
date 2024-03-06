package com.harana.modules.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, Handler, Vertx => VX}
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.sockjs.{SockJSBridgeOptions, SockJSHandler}
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import org.jose4j.jwk.{JsonWebKey, JsonWebKeySet}
import org.pac4j.core.config.Config
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.exception.http.HttpAction
import org.pac4j.core.util.Pac4jConstants
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.vertx.VertxWebContext
import org.pac4j.vertx.handler.impl.{LogoutHandler, LogoutHandlerOptions}

import javax.ws.rs.core.{HttpHeaders, MediaType}

object Handlers {

  def sock(vx: VX, inboundPermitted: List[String], outboundPermitted: List[String]) = {
    val bridgeOptions = new SockJSBridgeOptions()
    inboundPermitted.foreach(regex => bridgeOptions.addInboundPermitted(new PermittedOptions().setAddressRegex(regex)))
    outboundPermitted.foreach(regex => bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddressRegex(regex)))
    SockJSHandler.create(vx).bridge(bridgeOptions)
  }


  def jwks(jwks: JsonWebKeySet): Handler[RoutingContext] = {
    rc: RoutingContext => {
      rc.response
        .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .end(jwks.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
    }
  }


  def forceLogin(config: Config, sessionStore: SessionStore): Handler[RoutingContext] = {
    rc: RoutingContext => {
      val context = new VertxWebContext(rc, sessionStore)
      try {
        val client = config.getClients.findClient(context.getRequestParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER).get)
        val action = client.get.getRedirectionAction(context, sessionStore)
        val adapter = config.getHttpActionAdapter
        adapter.adapt(action.get, context)
      } catch {
        case h: HttpAction => rc.fail(h)
      }
    }
  }


  def loginForm(vx: VX, config: Config, templateFileName: String, parameters: Map[String, AnyRef]): Handler[RoutingContext] = {
    rc: RoutingContext => {
      val url = config.getClients.findClient("FormClient").get.asInstanceOf[FormClient].getCallbackUrl
      template(vx, rc, templateFileName, parameters ++ Map("url" -> url))
    }
  }


  def centralLogout(vx: VX, config: Config, sessionStore: SessionStore, postLogoutUrl: String): Handler[RoutingContext] = {
    val options = new LogoutHandlerOptions().setCentralLogout(true).setLocalLogout(false).setDefaultUrl(postLogoutUrl)
    new LogoutHandler(vx, sessionStore, options, config)
  }


  def template(vx: VX, rc: RoutingContext, templateFileName: String, parameters: Map[String, AnyRef]): Unit = {
    val engine = HandlebarsTemplateEngine.create(vx)
    val json = new JsonObject()
    parameters.foreach { p => json.put(p._1, p._2) }
    engine.render(json, templateFileName, new Handler[AsyncResult[Buffer]] {
      override def handle(result: AsyncResult[Buffer]): Unit = {
        if (result.succeeded()) rc.response.end(result.result()) else rc.fail(result.cause())
      }
    })
  }

}