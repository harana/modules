package com.harana.modules.auth0

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.UserFilter
import com.auth0.exception.{APIException, Auth0Exception, RateLimitException}
import com.auth0.json.auth._
import com.auth0.json.mgmt.roles.Role
import com.auth0.json.mgmt.users.User
import com.auth0.net.Request
import com.harana.modules.auth0.models.Auth0Error
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.{IO, UIO, ZIO, ZLayer}

import java.net.URL
import scala.jdk.CollectionConverters._

object LiveAuth0 {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveAuth0(config, logger, micrometer)
  }
}

case class LiveAuth0(config: Config, logger: Logger, micrometer: Micrometer) extends Auth0 {

  private val authApi = for {
    domain        <- config.string("auth0.domain", "")
    clientId      <- config.secret("auth0-client-id")
    clientSecret  <- config.secret("auth0-client-secret")
  } yield {
    AuthAPI.newBuilder(domain, clientId, clientSecret).build()
  }

  private val managementApi = for {
    apiToken        <- config.secret("auth0-api-token")
    domain          <- config.string("auth0.domain", "")
  } yield {
    ManagementAPI.newBuilder(domain, apiToken).build()
  }

  def authorizeUrl(redirectUri: String,
                   audience: Option[String] = None,
                   connection: Option[String] = None,
                   parameter: Option[(String, String)] = None,
                   responseType: Option[String] = None,
                   scope: Option[String] = None,
                   state: Option[String] = None): UIO[URL] =
    for {
      a <- authApi
      r <- ZIO.succeed {
        var b = a.authorizeUrl(redirectUri)
        if (audience.nonEmpty) b = b.withAudience(audience.get)
        if (audience.nonEmpty) b = b.withAudience(audience.get)
        if (connection.nonEmpty) b = b.withConnection(connection.get)
        if (parameter.nonEmpty) b = b.withParameter(parameter.get._1, parameter.get._2)
        if (responseType.nonEmpty) b = b.withResponseType(responseType.get)
        if (scope.nonEmpty) b = b.withScope(scope.get)
        if (state.nonEmpty) b = b.withState(state.get)
        new URL(b.build())
      }
    } yield r

  def logoutUrl(returnToUrl: String, setClientId: Boolean, useFederated: Option[Boolean] = None): UIO[URL] =
    for {
      a <- authApi
      r <- ZIO.succeed {
        var b = a.logoutUrl(returnToUrl, setClientId)
        if (useFederated.nonEmpty) b = b.useFederated(useFederated.get)
        new URL(b.build())
      }
    } yield r

  def userInfo(accessToken: String): IO[Auth0Error, UserInfo] =
    for {
      a <- authApi
      r <- execute(a.userInfo(accessToken))
    } yield r

  def resetPassword(email: String): IO[Auth0Error, Unit] =
    for {
      a <- authApi
      _ <- execute(a.resetPassword(email, "Username-Password-Authentication"))
      r <- ZIO.unit
    } yield r

  def signUp(email: String, username: Option[String], password: String): IO[Auth0Error, CreatedUser] =
    for {
      a <- authApi
      r <- execute(
        if (username.nonEmpty) a.signUp(email, username.get, password, "Username-Password-Authentication")
        else a.signUp(email, password, "Username-Password-Authentication")
      )
    } yield r

  def login(emailOrUsername: String, password: String, realm: Option[String]): IO[Auth0Error, TokenHolder] =
    for {
      a <- authApi
      r <- execute(
        if (realm.nonEmpty) a.login(emailOrUsername, password, realm.get)
        else a.login(emailOrUsername, password)
      )
    } yield r

  def requestToken(audience: String): IO[Auth0Error, TokenHolder] =
    for {
      a <- authApi
      r <- execute(a.requestToken(audience))
    } yield r

  def revokeToken(refreshToken: String): IO[Auth0Error, Unit] =
    for {
      a <- authApi
      _ <- execute(a.revokeToken(refreshToken))
      r <- ZIO.unit
    } yield r

  def renewAuth(refreshToken: String): IO[Auth0Error, TokenHolder] =
    for {
      a <- authApi
      r <- execute(a.renewAuth(refreshToken))
    } yield r

  def exchangeCode(code: String, redirectUri: String): IO[Auth0Error, TokenHolder] =
    for {
      a <- authApi
      r <- execute(a.exchangeCode(code, redirectUri))
    } yield r

  def listByEmail(email: String): IO[Auth0Error, List[User]] =
    for {
      m <- managementApi
      r <- execute(m.users.listByEmail(email, new UserFilter)).map(_.asScala.toList)
    } yield r

  def getUser(id: String): IO[Auth0Error, User] =
    for {
      m <- managementApi
      r <- execute(m.users.get(id, new UserFilter()))
    } yield r

  def createUser(user: User): IO[Auth0Error, User] =
    for {
      m <- managementApi
      r <- execute(m.users.create(user))
    } yield r

  def deleteUser(id: String): IO[Auth0Error, Unit] =
    for {
      m <- managementApi
      _ <- execute(m.users.delete(id))
      r <- ZIO.unit
    } yield r

  def updateUser(id: String, user: User): IO[Auth0Error, User] =
    for {
      m <- managementApi
      r <- execute(m.users.update(id, user))
    } yield r

  def getRole(id: String): IO[Auth0Error, Role] =
    for {
      m <- managementApi
      r <- execute(m.roles.get(id))
    } yield r

  def createRole(role: Role): IO[Auth0Error, Role] =
    for {
      m <- managementApi
      r <- execute(m.roles.create(role))
    } yield r

  def deleteRole(id: String): IO[Auth0Error, Unit] =
    for {
      m <- managementApi
      _ <- execute(m.roles.delete(id))
      r <- ZIO.unit
    } yield r

  def updateRole(id: String, role: Role): IO[Auth0Error, Role] =
    for {
      m <- managementApi
      r <- execute(m.roles.update(id, role))
    } yield r

  def assignUsersToRole(roleId: String, userIds: List[String]): IO[Auth0Error, Unit] =
    for {
      m <- managementApi
      _ <- execute(m.roles.assignUsers(roleId, userIds.asJava))
      r <- ZIO.unit
    } yield r

  private def execute[T](request: Request[T]): IO[Auth0Error, T] =
    ZIO.attempt(request).mapBoth({
      case e: RateLimitException => Auth0Error.RateLimit(e)
      case e: APIException => Auth0Error.Api(e)
      case e: Auth0Exception => Auth0Error.Request(e)
    }, _.execute().getBody)

}