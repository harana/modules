package com.harana.modules.email

import org.apache.commons.mail.EmailException
import zio.IO
import zio.macros.accessible

@accessible
trait Email {

  def isValid(email: String): Boolean

  def domain(email: String): String

  def obfuscate(email: String): String

  def send(message: EmailMessage): IO[EmailException, String]

}