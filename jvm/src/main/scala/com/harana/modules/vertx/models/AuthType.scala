package com.harana.modules.vertx.models

import io.vertx.core.{Vertx => VX}
import org.pac4j.cas.client.CasClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.core.client.Client
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.profile.creator.ProfileCreator
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.oauth.client.Google2Client.Google2Scope
import org.pac4j.oauth.client.QQClient.QQScope
import org.pac4j.oauth.client.WechatClient.WechatScope
import org.pac4j.oauth.client.WeiboClient.WeiboScope
import org.pac4j.oauth.client._
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration

import java.io.File
import scala.jdk.CollectionConverters._

sealed trait AuthType
object AuthType {

  case class Bitbucket(key: String, secret: String) extends AuthType
  case class Dropbox(key: String, secret: String) extends AuthType
  case class Facebook(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Github(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Google(key: String, secret: String, scope: Option[Google2Scope] = None) extends AuthType
  case class HiOrg(key: String, secret: String) extends AuthType
  case class Linkedin(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Odnoklassniki(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Paypal(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class QQ(key: String, secret: String, scope: List[QQScope] = List()) extends AuthType
  case class Strava(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Twitter(key: String, secret: String, includeEmail: Boolean) extends AuthType
  case class Vk(key: String, secret: String, scope: Option[String] = None) extends AuthType
  case class Wechat(key: String, secret: String, scopes: List[WechatScope] = List()) extends AuthType
  case class Weibo(key: String, secret: String, scope: Option[WeiboScope] = None) extends AuthType
  case class WindowsLive(key: String, secret: String) extends AuthType
  case class Wordpress(key: String, secret: String) extends AuthType
  case class Yahoo(key: String, secret: String) extends AuthType

  case class Basic(authenticator: Option[Authenticator]) extends AuthType

  case class CAS(url: String) extends AuthType

  case class Form(loginFormUrl: String,
                  authenticator: Authenticator,
                  profileCreator: ProfileCreator) extends AuthType

  case class JWT(salt: String) extends AuthType

  case class OIDC(clientId: String,
                  secret: String,
                  discoveryUri: String,
                  customParams: Map[String, String] = Map()) extends AuthType

  case class SAML(keystore: File,
                  keystorePassword: String,
                  privateKeyPassword: String,
                  identityProviderMetadataResource: String,
                  maximumAuthenticationLifetime: Int,
                  serviceProviderEntityId: String,
                  serviceProviderMetadata: File) extends AuthType

  def getClient(vx: VX, baseUrl: String, authType: AuthType): Client =
    authType match {
      case AuthType.Bitbucket(key, secret) =>
        new BitbucketClient(key, secret)

      case AuthType.Dropbox(key, secret) =>
        new DropBoxClient(key, secret)

      case AuthType.Facebook(key, secret, scope) =>
        val client = new FacebookClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.Github(key, secret, scope) =>
        val client = new GitHubClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.Google(key, secret, scope) =>
        val client = new Google2Client(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.HiOrg(key, secret) =>
        new HiOrgServerClient(key, secret)

      case AuthType.Linkedin(key, secret, scope) =>
        val client = new LinkedIn2Client(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.Odnoklassniki(key, secret, publicKey) =>
        new OkClient(key, secret, publicKey.orNull)

      case AuthType.Paypal(key, secret, scope) =>
        val client = new PayPalClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.QQ(key, secret, scopes) =>
        val client = new QQClient(key, secret)
        client.setScopes(scopes.asJava)
        client

      case AuthType.Strava(key, secret, scope) =>
        val client = new StravaClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.Twitter(key, secret, includeEmail) =>
        new TwitterClient(key, secret, includeEmail)

      case AuthType.Vk(key, secret, scope) =>
        val client = new VkClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.Wechat(key, secret, scopes) =>
        val client = new WechatClient(key, secret)
        client.setScopes(scopes.asJava)
        client

      case AuthType.Weibo(key, secret, scope) =>
        val client = new WeiboClient(key, secret)
        client.setScope(scope.orNull)
        client

      case AuthType.WindowsLive(key, secret) =>
        new WindowsLiveClient(key, secret)

      case AuthType.Wordpress(key, secret) =>
        new WordPressClient(key, secret)

      case AuthType.Yahoo(key, secret) =>
        new YahooClient(key, secret)

      case AuthType.Basic(authenticator) =>
        new DirectBasicAuthClient(authenticator.getOrElse(new SimpleTestUsernamePasswordAuthenticator()))

      case AuthType.CAS(url) =>
        val cfg = new CasConfiguration(url)
//        cfg.setLogoutHandler(new LogoutHandler(vx, new VertxLocalMapStore[String, AnyRef](vx), false))
        new CasClient(cfg)

      case AuthType.Form(loginFormUrl, authenticator, profileCreator) =>
        new FormClient(loginFormUrl, authenticator, profileCreator)

      case AuthType.JWT(salt) =>
        val parameterClient = new ParameterClient("token", new JwtAuthenticator(new SecretSignatureConfiguration(salt)))
        parameterClient.setSupportGetRequest(true)
        parameterClient.setSupportPostRequest(false)
        parameterClient

      case AuthType.OIDC(clientId, secret, discoveryUri, customParams) =>
        val cfg = new OidcConfiguration
        cfg.setClientId(clientId)
        cfg.setSecret(secret)
        cfg.setDiscoveryURI(discoveryUri)
        customParams.foreach { case (k, v) => cfg.addCustomParam(k, v)}
        new OidcClient(cfg)

      case AuthType.SAML(keystore, keystorePassword, privateKeyPassword, identityProviderMetadataResource,
      maximumAuthenticationLifetime, serviceProviderEntityId, serviceProviderMetadata) =>
        val cfg = new SAML2Configuration(keystore.getAbsolutePath, keystorePassword, privateKeyPassword, identityProviderMetadataResource)
        cfg.setMaximumAuthenticationLifetime(maximumAuthenticationLifetime)
        cfg.setServiceProviderEntityId(serviceProviderEntityId)
        cfg.setServiceProviderMetadataPath(serviceProviderMetadata.getAbsolutePath)
        new SAML2Client(cfg)
    }
}