package com.harana.modules.email

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.email.models.{EmailAddress => hrmcEmailAddress}
import org.apache.commons.mail.{EmailAttachment, EmailException, HtmlEmail, MultiPartEmail, SimpleEmail}
import zio.{IO, ZIO, ZLayer}

import javax.mail.internet.InternetAddress
import scala.jdk.CollectionConverters._

object LiveEmail {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveEmail(config, logger, micrometer)
  }
}

case class LiveEmail(config: Config, logger: Logger, micrometer: Micrometer) extends Email {

  def isValid(email: String): Boolean =
    hrmcEmailAddress.isValid(email)

  def domain(email: String): String =
    hrmcEmailAddress.Domain(email)

  def obfuscate(email: String): String =
    hrmcEmailAddress(email).obfuscated.value

  def send(message: EmailMessage): IO[EmailException, String] = {

    val format =
      if (message.attachments.nonEmpty) MultiPart
      else if (message.richMessage.nonEmpty) Rich
      else Plain

    val commonsMail = format match {
      case Plain => new SimpleEmail().setMsg(message.message)
      case Rich => new HtmlEmail().setHtmlMsg(message.richMessage.get).setTextMsg(message.message)
      case MultiPart =>
        val multipartEmail = new MultiPartEmail()
        message.attachments.foreach { file =>
          val attachment = new EmailAttachment()
          attachment.setPath(file.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(file.getName)
          multipartEmail.attach(attachment)
        }
        multipartEmail.setMsg(message.message)
    }

    message.to.foreach(ea => commonsMail.addTo(ea))
    message.cc.foreach(cc => commonsMail.addCc(cc))
    message.bcc.foreach(bcc => commonsMail.addBcc(bcc))

    for {
      host      <- config.secret("email-host")
      auth      <- config.boolean("email.useAuthentication", default = false)
      username  <- config.secret("email-username")
      password  <- config.secret("email-password")
      ssl       <- config.boolean("email.useSSL", default = true)
      port      <- config.int("email.port", if (ssl) 25 else 587)
    } yield {
      commonsMail.setHostName(host)
      if (auth) commonsMail.setAuthentication(username, password)
      commonsMail.setSSLOnConnect(ssl)
      commonsMail.setSmtpPort(port)
      commonsMail.setFrom(message.from._1, message.from._2)
      commonsMail.setSubject(message.subject)
      if (message.replyTo.nonEmpty) commonsMail.setReplyTo(List(new InternetAddress(message.replyTo.get)).asJava)
      commonsMail.send()
    }
  }
}