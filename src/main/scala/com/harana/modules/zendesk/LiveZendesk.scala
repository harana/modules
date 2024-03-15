package com.harana.modules.zendesk

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.zendesk.LiveZendesk.clientRef
import com.harana.modules.zendesk.models.ZendeskError
import org.zendesk.client.v2.Zendesk.Builder
import org.zendesk.client.v2.{Zendesk => CoreZendesk}
import org.zendesk.client.v2.model._
import org.zendesk.client.v2.model.hc._
import org.zendesk.client.v2.model.schedules.{Holiday, Schedule}
import org.zendesk.client.v2.model.targets.Target
import zio.{IO, ZIO, ZLayer}

import java.io.File
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

object LiveZendesk {
	private val clientRef = new AtomicReference[Option[CoreZendesk]](None)

	val layer = ZLayer {
		for {
			config        <- ZIO.service[Config]
			logger        <- ZIO.service[Logger]
			micrometer    <- ZIO.service[Micrometer]
		} yield LiveZendesk(config, logger, micrometer)
	}
}

case class LiveZendesk(config: Config, logger: Logger, micrometer: Micrometer) extends Zendesk {

	private def client =
		for {
			client         	<- if (clientRef.get.nonEmpty) ZIO.succeed(clientRef.get.get) else
													for {
															url 					<- config.string("zendesk.url")
															username 			<- config.secret("zendesk-username")
															password 			<- config.secret("zendesk-password")
															oauthToken 		<- config.secret("zendesk-oauth-token")
															token 				<- config.secret("zendesk-token")
													} yield {
															new Builder(url)
																.setUsername(username)
																.setPassword(password)
																.setOauthToken(oauthToken)
																.setToken(token)
																.build()
													}
			_                 =  clientRef.set(Some(client))
		} yield client


	def getBrands: IO[ZendeskError, List[Brand]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getBrands)
		} yield r


	def getTicketForm(id: Long): IO[ZendeskError, TicketForm] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketForm(id))
		} yield r


	def getTicketForms: IO[ZendeskError, List[TicketForm]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketForms)
		} yield r


	def createTicketForm(ticketForm: TicketForm): IO[ZendeskError, TicketForm] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTicketForm(ticketForm))
		} yield r


	def importTicket(ticketImport: TicketImport): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.importTicket(ticketImport))
		} yield r


	def importTickets(ticketImports: List[TicketImport]): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.importTicketsAsync(ticketImports))
		} yield r


	def getRecentTickets: IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRecentTickets)
		} yield r


	def getTickets: IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTickets)
		} yield r


	def getTicketsIncrementally(startDate: Date, endDate: Date): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketsIncrementally(startDate, endDate))
		} yield r


	def getTicketsByExternalId(externalId: String, includeArchived: Boolean): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketsByExternalId(externalId, includeArchived))
		} yield r


	def getTicketsFromSearch(searchTerm: String): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketsFromSearch(searchTerm))
		} yield r


	def getTicket(ticketId: Long): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicket(ticketId))
		} yield r


	def getTickets(ticketIds: List[Long]): IO[ZendeskError, List[Ticket]] =
		ticketIds.headOption match {
			case None => ZIO.attempt(List())
			case Some(x) =>
				for {
					c <- client
					r <- ZIO.attempt(c.getTickets(x, ticketIds.drop(1): _*))
				} yield r
		}


	def getTicketIncidents(ticketId: Long): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketIncidents(ticketId))
		} yield r


	def getTicketCollaborators(ticketId: Long): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketCollaborators(ticketId))
		} yield r


	def getOrganizationTickets(organizationId: Long): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationTickets(organizationId))
		} yield r


	def getUserRequestedTickets(userId: Long): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserRequestedTickets(userId))
		} yield r


	def permanentlyDeleteTicket(ticket: Ticket): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.permanentlyDeleteTicket(ticket)).map(_ => ())
		} yield r


	def permanentlyDeleteTicket(ticketId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.permanentlyDeleteTicket(ticketId)).map(_ => ())
		} yield r


	def deleteTicket(ticket: Ticket): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTicket(ticket))
		} yield r


	def deleteTicket(ticketId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTicket(ticketId))
		} yield r


	def createTicket(ticket: Ticket): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTicketAsync(ticket))
		} yield r


	def createTickets(tickets: List[Ticket]): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTicketsAsync(tickets))
		} yield r


	def updateTicket(ticket: Ticket): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateTicket(ticket))
		} yield r


	def updateTickets(tickets: List[Ticket]): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateTicketsAsync(tickets))
		} yield r


	def markTicketAsSpam(ticket: Ticket): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.markTicketAsSpam(ticket))
		} yield r


	def deleteTickets(ticketIds: List[Long]): IO[ZendeskError, Unit] =
		ticketIds.headOption match {
			case None => ZIO.unit
			case Some(x) =>
				for {
					c <- client
					r <- ZIO.attempt(c.deleteTickets(x, ticketIds.drop(1): _*)).mapError(handleException)
				} yield r
		}


	def permanentlyDeleteTickets(ticketIds: List[Long]): IO[ZendeskError, Unit] =
		ticketIds.headOption match {
			case None => ZIO.unit
			case Some(x) =>
				for {
					c <- client
					r <- ZIO.attempt(c.permanentlyDeleteTickets(x, ticketIds.drop(1): _*)).mapBoth(handleException, _ => ())
				} yield r
			}


	def getComplianceDeletionStatuses(userId: Long): IO[ZendeskError, List[ComplianceDeletionStatus]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getComplianceDeletionStatuses(userId))
		} yield r


	def getUserCCDTickets(userId: Long): IO[ZendeskError, List[Ticket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserCCDTickets(userId))
		} yield r


	def getUserRelatedInfo(userId: Long): IO[ZendeskError, UserRelatedInfo] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserRelatedInfo(userId))
		} yield r


	def getTicketMetrics: IO[ZendeskError, List[Metric]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketMetrics)
		} yield r


	def getTicketMetricByTicket(id: Long): IO[ZendeskError, Metric] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketMetricByTicket(id))
		} yield r


	def getTicketMetric(id: Long): IO[ZendeskError, Metric] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketMetric(id))
		} yield r


	def getTicketAudits(ticket: Ticket): IO[ZendeskError, List[Audit]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketAudits(ticket))
		} yield r


	def getTicketAudits(id: Long): IO[ZendeskError, List[Audit]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketAudits(id))
		} yield r


	def getTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Audit] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketAudit(ticket, audit))
		} yield r


	def getTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Audit] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketAudit(ticket, auditId))
		} yield r


	def getTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Audit] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketAudit(ticketId, auditId))
		} yield r


	def trustTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.trustTicketAudit(ticket, audit))
		} yield r


	def trustTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.trustTicketAudit(ticket, auditId))
		} yield r


	def trustTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.trustTicketAudit(ticketId, auditId))
		} yield r


	def makePrivateTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.makePrivateTicketAudit(ticket, audit))
		} yield r


	def makePrivateTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.makePrivateTicketAudit(ticket, auditId))
		} yield r


	def makePrivateTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.makePrivateTicketAudit(ticketId, auditId))
		} yield r


	def getTicketFields: IO[ZendeskError, List[Field]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketFields)
		} yield r


	def getTicketField(ticketFieldId: Long): IO[ZendeskError, Field] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketField(ticketFieldId))
		} yield r


	def createTicketField(field: Field): IO[ZendeskError, Field] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTicketField(field))
		} yield r


	def updateTicketField(field: Field): IO[ZendeskError, Field] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateTicketField(field))
		} yield r


	def deleteTicketField(field: Field): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTicketField(field))
		} yield r


	def deleteTicketField(ticketFieldId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTicketField(ticketFieldId))
		} yield r


	def getSuspendedTickets: IO[ZendeskError, List[SuspendedTicket]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSuspendedTickets)
		} yield r


	def deleteSuspendedTicket(ticket: SuspendedTicket): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteSuspendedTicket(ticket))
		} yield r


	def deleteSuspendedTicket(ticketId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteSuspendedTicket(ticketId))
		} yield r


	def createUpload(fileName: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUpload(fileName, content))
		} yield r


	def createUpload(fileName: String, contentType: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUpload(fileName, contentType, content))
		} yield r


	def createUpload(token: String, fileName: String, contentType: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUpload(token, fileName, contentType, content))
		} yield r


	def deleteUpload(upload: Attachment.Upload): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUpload(upload))
		} yield r


	def deleteUpload(token: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUpload(token))
		} yield r


	def getArticlesFromSearch(searchTerm: String): IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticleFromSearch(searchTerm))
		} yield r


	def getArticlesFromSearch(searchTerm: String, sectionId: Long): IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticleFromSearch(searchTerm, sectionId))
		} yield r


	def getAttachmentsFromArticle(articleId: Long): IO[ZendeskError, List[ArticleAttachments]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAttachmentsFromArticle(articleId))
		} yield r


	def associateAttachmentsToArticle(articleId: Long, attachment: List[Attachment]): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.associateAttachmentsToArticle(articleId.toString, attachment))
		} yield r


	def createUploadArticle(articleId: Long, file: File, inline: Boolean = false): IO[ZendeskError, ArticleAttachments] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUploadArticle(articleId, file, inline))
		} yield r


	def getAttachment(attachment: Attachment): IO[ZendeskError, Attachment] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAttachment(attachment))
		} yield r


	def getAttachment(attachmentId: Long): IO[ZendeskError, Attachment] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAttachment(attachmentId))
		} yield r


	def deleteAttachment(attachmentId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteAttachment(attachmentId))
		} yield r


	def getTargets: IO[ZendeskError, List[Target]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTargets)
		} yield r


	def getTarget(targetId: Long): IO[ZendeskError, Target] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTarget(targetId))
		} yield r


	def createTarget(target: Target): IO[ZendeskError, Target] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTarget(target: Target))
		} yield r


	def deleteTarget(targetId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTarget(targetId))
		} yield r


	def getTriggers: IO[ZendeskError, List[Trigger]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTriggers)
		} yield r


	def getTrigger(triggerId: Long): IO[ZendeskError, Trigger] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTrigger(triggerId))
		} yield r


	def createTrigger(trigger: Trigger): IO[ZendeskError, Trigger] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTrigger(trigger))
		} yield r


	def updateTrigger(triggerId: Long, trigger: Trigger): IO[ZendeskError, Trigger] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateTrigger(triggerId, trigger))
		} yield r


	def deleteTrigger(triggerId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTrigger(triggerId))
		} yield r


	def getAutomations: IO[ZendeskError, List[Automation]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAutomations)
		} yield r


	def getAutomation(id: Long): IO[ZendeskError, Automation] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAutomation(id))
		} yield r


	def createAutomation(automation: Automation): IO[ZendeskError, Automation] =
		for {
			c <- client
			r <- ZIO.attempt(c.createAutomation(automation))
		} yield r


	def updateAutomation(automationId: Long, automation: Automation): IO[ZendeskError, Automation] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateAutomation(automationId, automation))
		} yield r


	def deleteAutomation(automationId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteAutomation(automationId))
		} yield r


	def getTwitterMonitors: IO[ZendeskError, List[TwitterMonitor]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTwitterMonitors)
		} yield r


	def getUsers: IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUsers)
		} yield r


	def getUsersByRoles(roles: List[String]): IO[ZendeskError, List[User]] =
		roles.headOption match {
			case None => ZIO.attempt(List())
			case Some(x) =>
				for {
					c <- client
					r <- ZIO.attempt(c.getUsersByRole(x, roles.drop(1): _*))
				} yield r
		}


	def getUsersIncrementally(startTime: Date): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUsersIncrementally(startTime))
		} yield r


	def getGroupUsers(groupId: Long): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupUsers(groupId))
		} yield r


	def getOrganizationUsers(organizationId: Long): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationUsers(organizationId))
		} yield r


	def getUser(userId: Long): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUser(userId))
		} yield r


	def getAuthenticatedUser: IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAuthenticatedUser)
		} yield r


	def getUserFields: IO[ZendeskError, List[UserField]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserFields)
		} yield r


	def createUser(user: User): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUser(user))
		} yield r


	def createUsers(users: List[User]): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUsersAsync(users))
		} yield r


	def createOrUpdateUser(user: User): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.createOrUpdateUser(user))
		} yield r


	def updateUser(user: User): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateUser(user))
		} yield r


	def deleteUser(user: User): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUser(user))
		} yield r


	def deleteUser(userId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUser(userId))
		} yield r


	def permanentlyDeleteUser(user: User): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.permanentlyDeleteUser(user))
		} yield r


	def permanentlyDeleteUser(userId: Long): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.permanentlyDeleteUser(userId))
		} yield r


	def suspendUser(userId: Long): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.suspendUser(userId))
		} yield r


	def unsuspendUser(userId: Long): IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.unsuspendUser(userId))
		} yield r


	def lookupUserByEmail(email: String): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.lookupUserByEmail(email))
		} yield r


	def lookupUserByExternalId(externalId: String): IO[ZendeskError, List[User]] =
		for {
			c <- client
			r <- ZIO.attempt(c.lookupUserByExternalId(externalId))
		} yield r


	def getCurrentUser: IO[ZendeskError, User] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCurrentUser)
		} yield r


	def resetUserPassword(user: User, password: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.resetUserPassword(user, password))
		} yield r


	def resetUserPassword(userId: Long, password: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.resetUserPassword(userId, password))
		} yield r


	def changeUserPassword(user: User, oldPassword: String, newPassword: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.changeUserPassword(user, oldPassword, newPassword))
		} yield r


	def getUserIdentities(user: User): IO[ZendeskError, List[Identity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserIdentities(user))
		} yield r


	def getUserIdentities(userId: Long): IO[ZendeskError, List[Identity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserIdentities(userId))
		} yield r


	def getUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserIdentity(user, identity))
		} yield r


	def getUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserIdentity(user, identityId))
		} yield r


	def getUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserIdentity(userId, identityId))
		} yield r


	def setUserPrimaryIdentity(user: User, identity: Identity): IO[ZendeskError, List[Identity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setUserPrimaryIdentity(user, identity))
		} yield r


	def setUserPrimaryIdentity(user: User, identityId: Long): IO[ZendeskError, List[Identity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setUserPrimaryIdentity(user, identityId))
		} yield r


	def setUserPrimaryIdentity(userId: Long, identityId: Long): IO[ZendeskError, List[Identity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setUserPrimaryIdentity(userId, identityId))
		} yield r


	def verifyUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.verifyUserIdentity(user, identity))
		} yield r


	def verifyUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.verifyUserIdentity(user, identityId))
		} yield r


	def verifyUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.verifyUserIdentity(userId, identityId))
		} yield r


	def requestVerifyUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.requestVerifyUserIdentity(user, identity))
		} yield r


	def requestVerifyUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.requestVerifyUserIdentity(user, identityId))
		} yield r


	def requestVerifyUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.requestVerifyUserIdentity(userId, identityId))
		} yield r


	def updateUserIdentity(userId: Long, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateUserIdentity(userId, identity))
		} yield r


	def updateUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateUserIdentity(user, identity))
		} yield r


	def deleteUserIdentity(user: User, identity: Identity): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUserIdentity(user, identity))
		} yield r


	def deleteUserIdentity(user: User, identityId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUserIdentity(user, identityId))
		} yield r


	def deleteUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteUserIdentity(userId, identityId))
		} yield r


	def createUserIdentity(userId: Long, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUserIdentity(userId, identity))
		} yield r


	def createUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity] =
		for {
			c <- client
			r <- ZIO.attempt(c.createUserIdentity(user, identity))
		} yield r


	def getCustomAgentRoles: IO[ZendeskError, List[AgentRole]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCustomAgentRoles)
		} yield r


	def getRequests: IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequests)
		} yield r


	def getOpenRequests: IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOpenRequests)
		} yield r


	def getSolvedRequests: IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSolvedRequests)
		} yield r


	def getCCRequests: IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCCRequests)
		} yield r


	def getUserRequests(user: User): IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserRequests(user))
		} yield r


	def getUserRequests(id: Long): IO[ZendeskError, List[Request]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserRequests(id))
		} yield r


	def getRequest(id: Long): IO[ZendeskError, Request] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequest(id))
		} yield r


	def createRequest(request: Request): IO[ZendeskError, Request] =
		for {
			c <- client
			r <- ZIO.attempt(c.createRequest(request))
		} yield r


	def updateRequest(request: Request): IO[ZendeskError, Request] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateRequest(request))
		} yield r


	def getRequestComments(request: Request): IO[ZendeskError, List[Comment]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequestComments(request))
		} yield r


	def getRequestComments(id: Long): IO[ZendeskError, List[Comment]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequestComments(id))
		} yield r


	def getTicketComments(id: Long): IO[ZendeskError, List[Comment]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTicketComments(id))
		} yield r


	def getRequestComment(request: Request, comment: Comment): IO[ZendeskError, Comment] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequestComment(request, comment))
		} yield r


	def getRequestComment(request: Request, commentId: Long): IO[ZendeskError, Comment] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequestComment(request, commentId))
		} yield r


	def getRequestComment(requestId: Long, commentId: Long): IO[ZendeskError, Comment] =
		for {
			c <- client
			r <- ZIO.attempt(c.getRequestComment(requestId, commentId))
		} yield r


	def createComment(ticketId: Long, comment: Comment): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.createComment(ticketId, comment))
		} yield r


	def createTicketFromTweet(tweetId: Long, monitorId: Long): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTicketFromTweet(tweetId, monitorId))
		} yield r


	def getOrganizations: IO[ZendeskError, List[Organization]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizations)
		} yield r


	def getOrganizationsIncrementally(startTime: Date): IO[ZendeskError, List[Organization]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationsIncrementally(startTime))
		} yield r


	def getOrganizationFields: IO[ZendeskError, List[OrganizationField]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationFields)
		} yield r


	def getAutoCompleteOrganizations(name: String): IO[ZendeskError, List[Organization]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAutoCompleteOrganizations(name))
		} yield r


	def getOrganization(id: Long): IO[ZendeskError, Organization] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganization(id))
		} yield r


	def createOrganization(organization: Organization): IO[ZendeskError, Organization] =
		for {
			c <- client
			r <- ZIO.attempt(c.createOrganization(organization))
		} yield r


	def createOrganizations(organizations: List[Organization]): IO[ZendeskError, List[Organization]] =
		for {
			c <- client
			r <- ZIO.attempt(c.createOrganizationsAsync(organizations))
		} yield r


	def updateOrganization(organization: Organization): IO[ZendeskError, Organization] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateOrganization(organization))
		} yield r


	def deleteOrganization(organization: Organization): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteOrganization(organization))
		} yield r


	def deleteOrganization(id: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteOrganization(id))
		} yield r


	def lookupOrganizationsByExternalId(externalId: String): IO[ZendeskError, List[Organization]] =
		for {
			c <- client
			r <- ZIO.attempt(c.lookupOrganizationsByExternalId(externalId))
		} yield r


	def getOrganizationMemberships: IO[ZendeskError, List[OrganizationMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMemberships)
		} yield r


	def getOrganizationMembershipsForOrg(organizationId: Long): IO[ZendeskError, List[OrganizationMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMembershipsForOrg(organizationId))
		} yield r


	def getOrganizationMembershipsForUser(userId: Long): IO[ZendeskError, List[OrganizationMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMembershipsForUser(userId))
		} yield r


	def getOrganizationMembershipForUser(userId: Long, id: Long): IO[ZendeskError, OrganizationMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMembershipForUser(userId, id))
		} yield r


	def getOrganizationMembership(id: Long): IO[ZendeskError, OrganizationMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMembership(id))
		} yield r


	def createOrganizationMembership(organizationMembership: OrganizationMembership): IO[ZendeskError, OrganizationMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.createOrganizationMembership(organizationMembership: OrganizationMembership))
		} yield r


	def deleteOrganizationMembership(id: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteOrganizationMembership(id))
		} yield r


	def getGroups: IO[ZendeskError, List[Group]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroups)
		} yield r


	def getAssignableGroups: IO[ZendeskError, List[Group]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAssignableGroups)
		} yield r


	def getGroup(groupId: Long): IO[ZendeskError, Group] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroup(groupId))
		} yield r


	def createGroup(group: Group): IO[ZendeskError, Group] =
		for {
			c <- client
			r <- ZIO.attempt(c.createGroup(group))
		} yield r


	def updateGroup(group: Group): IO[ZendeskError, Group] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateGroup(group))
		} yield r


	def deleteGroup(group: Group): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroup(group))
		} yield r


	def deleteGroup(groupId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroup(groupId))
		} yield r


	def getMacros: IO[ZendeskError, List[Macro]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getMacros)
		} yield r


	def getMacro(macroId: Long): IO[ZendeskError, Macro] =
		for {
			c <- client
			r <- ZIO.attempt(c.getMacro(macroId))
		} yield r


	def createMacro(`macro`: Macro): IO[ZendeskError, Macro] =
		for {
			c <- client
			r <- ZIO.attempt(c.createMacro(`macro`))
		} yield r


	def updateMacro(macroId: Long, `macro`: Macro): IO[ZendeskError, Macro] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateMacro(macroId, `macro`))
		} yield r


	def macrosShowChangesToTicket(macroId: Long): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.macrosShowChangesToTicket(macroId))
		} yield r


	def macrosShowTicketAfterChanges(ticketId: Long, macroId: Long): IO[ZendeskError, Ticket] =
		for {
			c <- client
			r <- ZIO.attempt(c.macrosShowTicketAfterChanges(ticketId, macroId))
		} yield r


	def addTagToTicket(ticketId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.addTagToTicket(ticketId, tags: _*))
		} yield r


	def addTagToTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.addTagToTopics(topicId, tags: _*))
		} yield r


	def addTagToOrganization(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.addTagToOrganisations(organizationId, tags: _*))
		} yield r


	def setTagOnTicket(tagId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setTagOnTicket(tagId, tags: _*))
		} yield r


	def setTagOnTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setTagOnTopics(topicId, tags: _*))
		} yield r


	def setTagOnOrganisation(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setTagOnOrganisations(organizationId, tags: _*))
		} yield r


	def removeTagFromTicket(ticketId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.removeTagFromTicket(ticketId, tags: _*))
		} yield r


	def removeTagFromTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.removeTagFromTopics(topicId, tags: _*))
		} yield r


	def removeTagFromOrganisation(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.removeTagFromOrganisations(organizationId, tags: _*))
		} yield r


	def getIncrementalTicketsResult(unixEpochTime: Long): IO[ZendeskError, Map[Any, Any]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getIncrementalTicketsResult(unixEpochTime).asScala.toMap[Any, Any]).mapError(handleException)
		} yield r


	def getGroupMemberships: IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupMemberships)
		} yield r


	def getGroupMembershipByUser(userId: Long): IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupMembershipByUser(userId))
		} yield r


	def getGroupMemberships(groupId: Long): IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupMemberships(groupId))
		} yield r


	def getAssignableGroupMemberships: IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAssignableGroupMemberships)
		} yield r


	def getAssignableGroupMemberships(groupId: Long): IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getAssignableGroupMemberships(groupId))
		} yield r


	def getGroupMembership(groupMembershipId: Long): IO[ZendeskError, GroupMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupMembership(groupMembershipId))
		} yield r


	def getGroupMembership(userId: Long, groupMembershipId: Long): IO[ZendeskError, GroupMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupMembership(userId, groupMembershipId))
		} yield r


	def createGroupMembership(groupMembership: GroupMembership): IO[ZendeskError, GroupMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.createGroupMembership(groupMembership: GroupMembership))
		} yield r


	def createGroupMembership(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, GroupMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.createGroupMembership(userId, groupMembership))
		} yield r


	def deleteGroupMembership(groupMembership: GroupMembership): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroupMembership(groupMembership))
		} yield r


	def deleteGroupMembership(id: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroupMembership(id))
		} yield r


	def deleteGroupMembership(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroupMembership(userId, groupMembership))
		} yield r


	def deleteGroupMembership(userId: Long, groupMembershipId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteGroupMembership(userId, groupMembershipId))
		} yield r


	def setGroupMembershipAsDefault(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, List[GroupMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setGroupMembershipAsDefault(userId, groupMembership))
		} yield r


	def getForums: IO[ZendeskError, List[Forum]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getForums)
		} yield r


	def getForums(categoryId: Long): IO[ZendeskError, List[Forum]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getForums(categoryId))
		} yield r


	def getForum(forumId: Long): IO[ZendeskError, Forum] =
		for {
			c <- client
			r <- ZIO.attempt(c.getForum(forumId))
		} yield r


	def createForum(forum: Forum): IO[ZendeskError, Forum] =
		for {
			c <- client
			r <- ZIO.attempt(c.createForum(forum))
		} yield r


	def updateForum(forum: Forum): IO[ZendeskError, Forum] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateForum(forum))
		} yield r


	def deleteForum(forum: Forum): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteForum(forum))
		} yield r


	def getTopics: IO[ZendeskError, List[Topic]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTopics)
		} yield r


	def getTopics(forumId: Long): IO[ZendeskError, List[Topic]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTopics(forumId))
		} yield r


	def getTopics(topicIds: List[Long]): IO[ZendeskError, List[Topic]] =
		topicIds.headOption match {
			case None => ZIO.attempt(List())
			case Some(x) =>
				for {
					c <- client
					r <- ZIO.attempt(c.getTopics(x, topicIds.drop(1): _*))
			} yield r
		}


	def getTopicsByUser(userId: Long): IO[ZendeskError, List[Topic]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTopicsByUser(userId))
		} yield r


	def getTopic(topicId: Long): IO[ZendeskError, Topic] =
		for {
			c <- client
			r <- ZIO.attempt(c.getTopic(topicId))
		} yield r


	def createTopic(topic: Topic): IO[ZendeskError, Topic] =
		for {
			c <- client
			r <- ZIO.attempt(c.createTopic(topic))
		} yield r


	def importTopic(topic: Topic): IO[ZendeskError, Topic] =
		for {
			c <- client
			r <- ZIO.attempt(c.importTopic(topic))
		} yield r


	def updateTopic(topic: Topic): IO[ZendeskError, Topic] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateTopic(topic))
		} yield r


	def deleteTopic(topic: Topic): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteTopic(topic))
		} yield r


	def getOrganizationMembershipsByUser(userId: Long): IO[ZendeskError, List[OrganizationMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getOrganizationMembershipByUser(userId))
		} yield r


	def getGroupOrganization(userId: Long, organizationMembershipId: Long): IO[ZendeskError, OrganizationMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.getGroupOrganization(userId, organizationMembershipId))
		} yield r


	def createOrganizationMembership(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, OrganizationMembership] =
		for {
			c <- client
			r <- ZIO.attempt(c.createOrganizationMembership(userId, organizationMembership))
		} yield r


	def deleteOrganizationMembership(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteOrganizationMembership(userId, organizationMembership))
		} yield r


	def deleteOrganizationMembership(userId: Long, organizationMembershipId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteOrganizationMembership(userId, organizationMembershipId))
		} yield r


	def setOrganizationMembershipAsDefault(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, List[OrganizationMembership]] =
		for {
			c <- client
			r <- ZIO.attempt(c.setOrganizationMembershipAsDefault(userId, organizationMembership))
		} yield r


	def getSearchResults(query: String): IO[ZendeskError, List[SearchResultEntity]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSearchResults(query))
		} yield r


	def getSearchResults[T <: SearchResultEntity](cls: Class[T], query: String): IO[ZendeskError, List[T]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSearchResults[T](cls, query))
		} yield r


	def getSearchResults[T <: SearchResultEntity](cls: Class[T], query: String, params: String): IO[ZendeskError, List[T]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSearchResults[T](cls, query, params))
		} yield r


	def notifyApp(json: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.notifyApp(json))
		} yield r


	def updateInstallation(id: Int, json: String): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateInstallation(id, json))
		} yield r


	def getSatisfactionRatings: IO[ZendeskError, List[SatisfactionRating]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSatisfactionRatings)
		} yield r


	def getSatisfactionRating(id: Long): IO[ZendeskError, SatisfactionRating] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSatisfactionRating(id))
		} yield r


	def createSatisfactionRating(ticketId: Long, satisfactionRating: SatisfactionRating): IO[ZendeskError, SatisfactionRating] =
		for {
			c <- client
			r <- ZIO.attempt(c.createSatisfactionRating(ticketId, satisfactionRating))
		} yield r


	def createSatisfactionRating(ticket: Ticket, satisfactionRating: SatisfactionRating): IO[ZendeskError, SatisfactionRating] =
		for {
			c <- client
			r <- ZIO.attempt(c.createSatisfactionRating(ticket, satisfactionRating))
		} yield r


	def getHelpCenterLocales: IO[ZendeskError, List[String]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getHelpCenterLocales)
		} yield r


	def getArticles: IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticles)
		} yield r


	def getArticles(category: Category): IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticles(category))
		} yield r


	def getArticlesIncrementally(startTime: Date): IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticlesIncrementally(startTime))
		} yield r


	def getArticlesFromPage(page: Int): IO[ZendeskError, List[Article]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticlesFromPage(page))
		} yield r


	def getArticle(articleId: Long): IO[ZendeskError, Article] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticle(articleId))
		} yield r


	def getArticleTranslations(articleId: Long): IO[ZendeskError, List[Translation]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticleTranslations(articleId))
		} yield r


	def createArticle(article: Article): IO[ZendeskError, Article] =
		for {
			c <- client
			r <- ZIO.attempt(c.createArticle(article))
		} yield r


	def updateArticle(article: Article): IO[ZendeskError, Article] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateArticle(article))
		} yield r


	def createArticleTranslation(articleId: Long, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.createArticleTranslation(articleId, translation))
		} yield r


	def updateArticleTranslation(articleId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateArticleTranslation(articleId, locale, translation))
		} yield r


	def deleteArticle(article: Article): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteArticle(article))
		} yield r


	def deleteArticleAttachment(attachment: ArticleAttachments): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteArticleAttachment(attachment))
		} yield r


	def deleteArticleAttachment(attachmentId: Long): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteArticleAttachment(attachmentId))
		} yield r


	def getCategories: IO[ZendeskError, List[Category]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCategories)
		} yield r


	def getCategory(categoryId: Long): IO[ZendeskError, Category] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCategory(categoryId))
		} yield r


	def getCategoryTranslations(categoryId: Long): IO[ZendeskError, List[Translation]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getCategoryTranslations(categoryId))
		} yield r


	def createCategory(category: Category): IO[ZendeskError, Category] =
		for {
			c <- client
			r <- ZIO.attempt(c.createCategory(category))
		} yield r


	def updateCategory(category: Category): IO[ZendeskError, Category] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateCategory(category))
		} yield r


	def createCategoryTranslation(categoryId: Long, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.createCategoryTranslation(categoryId, translation))
		} yield r


	def updateCategoryTranslation(categoryId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateCategoryTranslation(categoryId, locale, translation))
		} yield r


	def deleteCategory(category: Category): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteCategory(category))
		} yield r


	def getSections: IO[ZendeskError, List[Section]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSections)
		} yield r


	def getSections(category: Category): IO[ZendeskError, List[Section]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSections(category))
		} yield r


	def getSection(sectionId: Long): IO[ZendeskError, Section] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSection(sectionId))
		} yield r


	def getSectionTranslations(sectionId: Long): IO[ZendeskError, List[Translation]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSectionTranslations(sectionId))
		} yield r


	def createSection(section: Section): IO[ZendeskError, Section] =
		for {
			c <- client
			r <- ZIO.attempt(c.createSection(section))
		} yield r


	def updateSection(section: Section): IO[ZendeskError, Section] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateSection(section))
		} yield r


	def createSectionTranslation(sectionId: Long, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.createSectionTranslation(sectionId, translation))
		} yield r


	def updateSectionTranslation(sectionId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation] =
		for {
			c <- client
			r <- ZIO.attempt(c.updateSectionTranslation(sectionId, locale, translation))
		} yield r


	def deleteSection(section: Section): IO[ZendeskError, Unit] =
		for {
			c <- client
			r <- ZIO.attempt(c.deleteSection(section))
		} yield r


	def getUserSubscriptions(user: User): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserSubscriptions(user))
		} yield r


	def getUserSubscriptions(userId: Long): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getUserSubscriptions(userId))
		} yield r


	def getArticleSubscriptions(articleId: Long): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticleSubscriptions(articleId))
		} yield r


	def getArticleSubscriptions(articleId: Long, locale: String): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getArticleSubscriptions(articleId, locale))
		} yield r


	def getSectionSubscriptions(sectionId: Long): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSectionSubscriptions(sectionId))
		} yield r


	def getSectionSubscriptions(sectionId: Long, locale: String): IO[ZendeskError, List[Subscription]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSectionSubscriptions(sectionId, locale))
		} yield r


	def getSchedules: IO[ZendeskError, List[Schedule]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSchedules)
		} yield r


	def getSchedule(schedule: Schedule): IO[ZendeskError, Schedule] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSchedule(schedule: Schedule))
		} yield r


	def getSchedule(scheduleId: Long): IO[ZendeskError, Schedule] =
		for {
			c <- client
			r <- ZIO.attempt(c.getSchedule(scheduleId))
		} yield r


	def getHolidaysForSchedule(schedule: Schedule): IO[ZendeskError, List[Holiday]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getHolidaysForSchedule(schedule).asScala.toList)
		} yield r


	def getHolidaysForSchedule(scheduleId: Long): IO[ZendeskError, List[Holiday]] =
		for {
			c <- client
			r <- ZIO.attempt(c.getHolidaysForSchedule(scheduleId))
		} yield r
}