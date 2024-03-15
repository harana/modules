package com.harana.modules.ldap

import zio.Task
import zio.macros.accessible

@accessible
trait Ldap {

  def createUser(emailAddress: String, password: String): Task[Unit]

  def deleteUser(emailAddress: String): Task[Unit]

  def setPassword(emailAddress: String, password: String): Task[Unit]

}