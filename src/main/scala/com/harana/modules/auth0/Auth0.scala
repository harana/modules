package com.harana.modules.auth0

import com.auth0.json.auth.{CreatedUser, TokenHolder, UserInfo}
import com.auth0.json.mgmt.roles.Role
import com.auth0.json.mgmt.users.User
import com.harana.modules.auth0.models.Auth0Error
import zio.IO
import zio.macros.accessible

import java.net.URL

@accessible
trait Auth0 {

    def authorizeUrl(redirectUri: String,
                     audience: Option[String] = None,
                     connection: Option[String] = None,
                     parameter: Option[(String, String)] = None,
                     responseType: Option[String] = None,
                     scope: Option[String] = None,
                     state: Option[String] = None): IO[Nothing, URL]

    def logoutUrl(returnToUrl: String, setClientId: Boolean, useFederated: Option[Boolean] = None): IO[Nothing, URL]

    def userInfo(accessToken: String): IO[Auth0Error, UserInfo]

    def resetPassword(email: String): IO[Auth0Error, Unit]

    def signUp(email: String, username: Option[String], password: String): IO[Auth0Error, CreatedUser]

    def login(emailOrUsername: String, password: String, realm: Option[String]): IO[Auth0Error, TokenHolder]

    def requestToken(audience: String): IO[Auth0Error, TokenHolder]

    def revokeToken(refreshToken: String): IO[Auth0Error, Unit]

    def renewAuth(refreshToken: String): IO[Auth0Error, TokenHolder]

    def exchangeCode(code: String, redirectUri: String): IO[Auth0Error, TokenHolder]

    def listByEmail(email: String): IO[Auth0Error, List[User]]

    def getUser(id: String): IO[Auth0Error, User]

    def createUser(user: User): IO[Auth0Error, User]

    def deleteUser(id: String): IO[Auth0Error, Unit]

    def updateUser(id: String, user: User): IO[Auth0Error, User]

    def getRole(id: String): IO[Auth0Error, Role]

    def createRole(role: Role): IO[Auth0Error, Role]

    def deleteRole(id: String): IO[Auth0Error, Unit]

    def updateRole(id: String, role: Role): IO[Auth0Error, Role]

    def assignUsersToRole(roleId: String, userIds: List[String]): IO[Auth0Error, Unit]

}