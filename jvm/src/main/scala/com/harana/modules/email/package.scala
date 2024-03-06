package com.harana.modules

import com.harana.modules.email.models.EmailAddress

import java.io.File

package object email {

  sealed abstract class MailType
  case object Plain extends MailType
  case object Rich extends MailType
  case object MultiPart extends MailType

  case class EmailMessage(from: (EmailAddress, String),
                          replyTo: Option[EmailAddress] = None,
                          to: List[EmailAddress],
                          cc: List[EmailAddress] = List(),
                          bcc: List[EmailAddress] = List(),
                          subject: String,
                          message: String,
                          richMessage: Option[String] = None,
                          attachments: List[File] = List())
}