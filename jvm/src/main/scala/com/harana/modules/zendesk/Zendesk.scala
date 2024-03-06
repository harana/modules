package com.harana.modules.zendesk

import com.harana.modules.zendesk.models.ZendeskError
import org.zendesk.client.v2.model._
import org.zendesk.client.v2.model.hc._
import org.zendesk.client.v2.model.schedules.{Holiday, Schedule}
import org.zendesk.client.v2.model.targets._
import zio.IO
import zio.macros.accessible

import java.io.File
import java.util.Date

@accessible
trait Zendesk {

    def getBrands: IO[ZendeskError, List[Brand]]

    def getTicketForm(id: Long): IO[ZendeskError, TicketForm]

    def getTicketForms: IO[ZendeskError, List[TicketForm]]

    def createTicketForm(ticketForm: TicketForm): IO[ZendeskError, TicketForm]

    def importTicket(ticketImport: TicketImport): IO[ZendeskError, Ticket]

    def importTickets(ticketImports: List[TicketImport]): IO[ZendeskError, List[Ticket]]

    def getRecentTickets: IO[ZendeskError, List[Ticket]]

    def getTickets: IO[ZendeskError, List[Ticket]]

    def getTicketsIncrementally(startDate: Date, endDate: Date): IO[ZendeskError, List[Ticket]]

    def getTicketsByExternalId(externalId: String, includeArchived: Boolean): IO[ZendeskError, List[Ticket]]

    def getTicketsFromSearch(searchTerm: String): IO[ZendeskError, List[Ticket]]

    def getTicket(ticketId: Long): IO[ZendeskError, Ticket]

    def getTickets(ticketIds: List[Long]): IO[ZendeskError, List[Ticket]]

    def getTicketIncidents(ticketId: Long): IO[ZendeskError, List[Ticket]]

    def getTicketCollaborators(ticketId: Long): IO[ZendeskError, List[User]]

    def getOrganizationTickets(organizationId: Long): IO[ZendeskError, List[Ticket]]

    def getUserRequestedTickets(userId: Long): IO[ZendeskError, List[Ticket]]

    def permanentlyDeleteTicket(ticket: Ticket): IO[ZendeskError, Unit]

    def permanentlyDeleteTicket(ticketId: Long): IO[ZendeskError, Unit]

    def deleteTicket(ticket: Ticket): IO[ZendeskError, Unit]

    def deleteTicket(ticketId: Long): IO[ZendeskError, Unit]

    def createTicket(ticket: Ticket): IO[ZendeskError, Ticket]

    def createTickets(tickets: List[Ticket]): IO[ZendeskError, List[Ticket]]

    def updateTicket(ticket: Ticket): IO[ZendeskError, Ticket]

    def updateTickets(tickets: List[Ticket]): IO[ZendeskError, List[Ticket]]

    def markTicketAsSpam(ticket: Ticket): IO[ZendeskError, Unit]

    def deleteTickets(ticketIds: List[Long]): IO[ZendeskError, Unit]

    def permanentlyDeleteTickets(ticketIds: List[Long]): IO[ZendeskError, Unit]

    def getComplianceDeletionStatuses(userId: Long): IO[ZendeskError, List[ComplianceDeletionStatus]]

    def getUserCCDTickets(userId: Long): IO[ZendeskError, List[Ticket]]

    def getUserRelatedInfo(userId: Long): IO[ZendeskError, UserRelatedInfo]

    def getTicketMetrics: IO[ZendeskError, List[Metric]]

    def getTicketMetricByTicket(id: Long): IO[ZendeskError, Metric]

    def getTicketMetric(id: Long): IO[ZendeskError, Metric]

    def getTicketAudits(ticket: Ticket): IO[ZendeskError, List[Audit]]

    def getTicketAudits(id: Long): IO[ZendeskError, List[Audit]]

    def getTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Audit]

    def getTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Audit]

    def getTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Audit]

    def trustTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Unit]

    def trustTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Unit]

    def trustTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Unit]

    def makePrivateTicketAudit(ticket: Ticket, audit: Audit): IO[ZendeskError, Unit]

    def makePrivateTicketAudit(ticket: Ticket, auditId: Long): IO[ZendeskError, Unit]

    def makePrivateTicketAudit(ticketId: Long, auditId: Long): IO[ZendeskError, Unit]

    def getTicketFields: IO[ZendeskError, List[Field]]

    def getTicketField(ticketFieldId: Long): IO[ZendeskError, Field]

    def createTicketField(field: Field): IO[ZendeskError, Field]

    def updateTicketField(field: Field): IO[ZendeskError, Field]

    def deleteTicketField(field: Field): IO[ZendeskError, Unit]

    def deleteTicketField(ticketFieldId: Long): IO[ZendeskError, Unit]

    def getSuspendedTickets: IO[ZendeskError, List[SuspendedTicket]]

    def deleteSuspendedTicket(ticket: SuspendedTicket): IO[ZendeskError, Unit]

    def deleteSuspendedTicket(ticketId: Long): IO[ZendeskError, Unit]

    def createUpload(fileName: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload]

    def createUpload(fileName: String, contentType: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload]

    def createUpload(token: String, fileName: String, contentType: String, content: Array[Byte]): IO[ZendeskError, Attachment.Upload]

    def deleteUpload(upload: Attachment.Upload): IO[ZendeskError, Unit]

    def deleteUpload(token: String): IO[ZendeskError, Unit]

    def getArticlesFromSearch(searchTerm: String): IO[ZendeskError, List[Article]]

    def getArticlesFromSearch(searchTerm: String, sectionId: Long): IO[ZendeskError, List[Article]]

    def getAttachmentsFromArticle(articleId: Long): IO[ZendeskError, List[ArticleAttachments]]

    def associateAttachmentsToArticle(articleId: Long, attachment: List[Attachment]): IO[ZendeskError, Unit]

    def createUploadArticle(articleId: Long, file: File, inline: Boolean): IO[ZendeskError, ArticleAttachments]

    def getAttachment(attachment: Attachment): IO[ZendeskError, Attachment]

    def getAttachment(attachmentId: Long): IO[ZendeskError, Attachment]

    def deleteAttachment(attachmentId: Long): IO[ZendeskError, Unit]

    def getTargets: IO[ZendeskError, List[Target]]

    def getTarget(targetId: Long): IO[ZendeskError, Target]

    def createTarget(target: Target): IO[ZendeskError, Target]

    def deleteTarget(targetId: Long): IO[ZendeskError, Unit]

    def getTriggers: IO[ZendeskError, List[Trigger]]

    def getTrigger(triggerId: Long): IO[ZendeskError, Trigger]

    def createTrigger(trigger: Trigger): IO[ZendeskError, Trigger]

    def updateTrigger(triggerId: Long, trigger: Trigger): IO[ZendeskError, Trigger]

    def deleteTrigger(triggerId: Long): IO[ZendeskError, Unit]

    def getAutomations: IO[ZendeskError, List[Automation]]

    def getAutomation(id: Long): IO[ZendeskError, Automation]

    def createAutomation(automation: Automation): IO[ZendeskError, Automation]

    def updateAutomation(automationId: Long, automation: Automation): IO[ZendeskError, Automation]

    def deleteAutomation(automationId: Long): IO[ZendeskError, Unit]

    def getTwitterMonitors: IO[ZendeskError, List[TwitterMonitor]]

    def getUsers: IO[ZendeskError, List[User]]

    def getUsersByRoles(roles: List[String]): IO[ZendeskError, List[User]]

    def getUsersIncrementally(startTime: Date): IO[ZendeskError, List[User]]

    def getGroupUsers(groupId: Long): IO[ZendeskError, List[User]]

    def getOrganizationUsers(organizationId: Long): IO[ZendeskError, List[User]]

    def getUser(userId: Long): IO[ZendeskError, User]

    def getAuthenticatedUser: IO[ZendeskError, User]

    def getUserFields: IO[ZendeskError, List[UserField]]

    def createUser(user: User): IO[ZendeskError, User]

    def createUsers(users: List[User]): IO[ZendeskError, List[User]]

    def createOrUpdateUser(user: User): IO[ZendeskError, User]

    def updateUser(user: User): IO[ZendeskError, User]

    def deleteUser(user: User): IO[ZendeskError, Unit]

    def deleteUser(userId: Long): IO[ZendeskError, Unit]

    def permanentlyDeleteUser(user: User): IO[ZendeskError, User]

    def permanentlyDeleteUser(userId: Long): IO[ZendeskError, User]

    def suspendUser(userId: Long): IO[ZendeskError, User]

    def unsuspendUser(userId: Long): IO[ZendeskError, User]

    def lookupUserByEmail(email: String): IO[ZendeskError, List[User]]

    def lookupUserByExternalId(externalId: String): IO[ZendeskError, List[User]]

    def getCurrentUser: IO[ZendeskError, User]

    def resetUserPassword(user: User, password: String): IO[ZendeskError, Unit]

    def resetUserPassword(userId: Long, password: String): IO[ZendeskError, Unit]

    def changeUserPassword(user: User, oldPassword: String, newPassword: String): IO[ZendeskError, Unit]

    def getUserIdentities(user: User): IO[ZendeskError, List[Identity]]

    def getUserIdentities(userId: Long): IO[ZendeskError, List[Identity]]

    def getUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity]

    def getUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity]

    def getUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity]

    def setUserPrimaryIdentity(user: User, identity: Identity): IO[ZendeskError, List[Identity]]

    def setUserPrimaryIdentity(user: User, identityId: Long): IO[ZendeskError, List[Identity]]

    def setUserPrimaryIdentity(userId: Long, identityId: Long): IO[ZendeskError, List[Identity]]

    def verifyUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity]

    def verifyUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity]

    def verifyUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity]

    def requestVerifyUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity]

    def requestVerifyUserIdentity(user: User, identityId: Long): IO[ZendeskError, Identity]

    def requestVerifyUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Identity]

    def updateUserIdentity(userId: Long, identity: Identity): IO[ZendeskError, Identity]

    def updateUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity]

    def deleteUserIdentity(user: User, identity: Identity): IO[ZendeskError, Unit]

    def deleteUserIdentity(user: User, identityId: Long): IO[ZendeskError, Unit]

    def deleteUserIdentity(userId: Long, identityId: Long): IO[ZendeskError, Unit]

    def createUserIdentity(userId: Long, identity: Identity): IO[ZendeskError, Identity]

    def createUserIdentity(user: User, identity: Identity): IO[ZendeskError, Identity]

    def getCustomAgentRoles: IO[ZendeskError, List[AgentRole]]

    def getRequests: IO[ZendeskError, List[Request]]

    def getOpenRequests: IO[ZendeskError, List[Request]]

    def getSolvedRequests: IO[ZendeskError, List[Request]]

    def getCCRequests: IO[ZendeskError, List[Request]]

    def getUserRequests(user: User): IO[ZendeskError, List[Request]]

    def getUserRequests(id: Long): IO[ZendeskError, List[Request]]

    def getRequest(id: Long): IO[ZendeskError, Request]

    def createRequest(request: Request): IO[ZendeskError, Request]

    def updateRequest(request: Request): IO[ZendeskError, Request]

    def getRequestComments(request: Request): IO[ZendeskError, List[Comment]]

    def getRequestComments(id: Long): IO[ZendeskError, List[Comment]]

    def getTicketComments(id: Long): IO[ZendeskError, List[Comment]]

    def getRequestComment(request: Request, comment: Comment): IO[ZendeskError, Comment]

    def getRequestComment(request: Request, commentId: Long): IO[ZendeskError, Comment]

    def getRequestComment(requestId: Long, commentId: Long): IO[ZendeskError, Comment]

    def createComment(ticketId: Long, comment: Comment): IO[ZendeskError, Ticket]

    def createTicketFromTweet(tweetId: Long, monitorId: Long): IO[ZendeskError, Ticket]

    def getOrganizations: IO[ZendeskError, List[Organization]]

    def getOrganizationsIncrementally(startTime: Date): IO[ZendeskError, List[Organization]]

    def getOrganizationFields: IO[ZendeskError, List[OrganizationField]]

    def getAutoCompleteOrganizations(name: String): IO[ZendeskError, List[Organization]]

    def getOrganization(id: Long): IO[ZendeskError, Organization]

    def createOrganization(organization: Organization): IO[ZendeskError, Organization]

    def createOrganizations(organizations: List[Organization]): IO[ZendeskError, List[Organization]]

    def updateOrganization(organization: Organization): IO[ZendeskError, Organization]

    def deleteOrganization(organization: Organization): IO[ZendeskError, Unit]

    def deleteOrganization(id: Long): IO[ZendeskError, Unit]

    def lookupOrganizationsByExternalId(externalId: String): IO[ZendeskError, List[Organization]]

    def getOrganizationMemberships: IO[ZendeskError, List[OrganizationMembership]]

    def getOrganizationMembershipsForOrg(organizationId: Long): IO[ZendeskError, List[OrganizationMembership]]

    def getOrganizationMembershipsForUser(userId: Long): IO[ZendeskError, List[OrganizationMembership]]

    def getOrganizationMembershipForUser(userId: Long, id: Long): IO[ZendeskError, OrganizationMembership]

    def getOrganizationMembership(id: Long): IO[ZendeskError, OrganizationMembership]

    def createOrganizationMembership(organizationMembership: OrganizationMembership): IO[ZendeskError, OrganizationMembership]

    def deleteOrganizationMembership(id: Long): IO[ZendeskError, Unit]

    def getGroups: IO[ZendeskError, List[Group]]

    def getAssignableGroups: IO[ZendeskError, List[Group]]

    def getGroup(groupId: Long): IO[ZendeskError, Group]

    def createGroup(group: Group): IO[ZendeskError, Group]

    def updateGroup(group: Group): IO[ZendeskError, Group]

    def deleteGroup(group: Group): IO[ZendeskError, Unit]

    def deleteGroup(groupId: Long): IO[ZendeskError, Unit]

    def getMacros: IO[ZendeskError, List[Macro]]

    def getMacro(macroId: Long): IO[ZendeskError, Macro]

    def createMacro(`macro`: Macro): IO[ZendeskError, Macro]

    def updateMacro(macroId: Long, `macro`: Macro): IO[ZendeskError, Macro]

    def macrosShowChangesToTicket(macroId: Long): IO[ZendeskError, Ticket]

    def macrosShowTicketAfterChanges(ticketId: Long, macroId: Long): IO[ZendeskError, Ticket]

    def addTagToTicket(ticketId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def addTagToTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def addTagToOrganization(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def setTagOnTicket(tagId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def setTagOnTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def setTagOnOrganisation(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def removeTagFromTicket(ticketId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def removeTagFromTopic(topicId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def removeTagFromOrganisation(organizationId: Long, tags: List[String]): IO[ZendeskError, List[String]]

    def getIncrementalTicketsResult(unixEpochTime: Long): IO[ZendeskError, Map[_, _]]

    def getGroupMemberships: IO[ZendeskError, List[GroupMembership]]

    def getGroupMembershipByUser(userId: Long): IO[ZendeskError, List[GroupMembership]]

    def getGroupMemberships(groupId: Long): IO[ZendeskError, List[GroupMembership]]

    def getAssignableGroupMemberships: IO[ZendeskError, List[GroupMembership]]

    def getAssignableGroupMemberships(groupId: Long): IO[ZendeskError, List[GroupMembership]]

    def getGroupMembership(groupMembershipId: Long): IO[ZendeskError, GroupMembership]

    def getGroupMembership(userId: Long, groupMembershipId: Long): IO[ZendeskError, GroupMembership]

    def createGroupMembership(groupMembership: GroupMembership): IO[ZendeskError, GroupMembership]

    def createGroupMembership(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, GroupMembership]

    def deleteGroupMembership(groupMembership: GroupMembership): IO[ZendeskError, Unit]

    def deleteGroupMembership(id: Long): IO[ZendeskError, Unit]

    def deleteGroupMembership(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, Unit]

    def deleteGroupMembership(userId: Long, groupMembershipId: Long): IO[ZendeskError, Unit]

    def setGroupMembershipAsDefault(userId: Long, groupMembership: GroupMembership): IO[ZendeskError, List[GroupMembership]]

    def getForums: IO[ZendeskError, List[Forum]]

    def getForums(categoryId: Long): IO[ZendeskError, List[Forum]]

    def getForum(forumId: Long): IO[ZendeskError, Forum]

    def createForum(forum: Forum): IO[ZendeskError, Forum]

    def updateForum(forum: Forum): IO[ZendeskError, Forum]

    def deleteForum(forum: Forum): IO[ZendeskError, Unit]

    def getTopics: IO[ZendeskError, List[Topic]]

    def getTopics(forumId: Long): IO[ZendeskError, List[Topic]]

    def getTopics(topicIds: List[Long]): IO[ZendeskError, List[Topic]]

    def getTopicsByUser(userId: Long): IO[ZendeskError, List[Topic]]

    def getTopic(topicId: Long): IO[ZendeskError, Topic]

    def createTopic(topic: Topic): IO[ZendeskError, Topic]

    def importTopic(topic: Topic): IO[ZendeskError, Topic]

    def updateTopic(topic: Topic): IO[ZendeskError, Topic]

    def deleteTopic(topic: Topic): IO[ZendeskError, Unit]

    def getOrganizationMembershipsByUser(userId: Long): IO[ZendeskError, List[OrganizationMembership]]

    def getGroupOrganization(userId: Long, organizationMembershipId: Long): IO[ZendeskError, OrganizationMembership]

    def createOrganizationMembership(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, OrganizationMembership]

    def deleteOrganizationMembership(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, Unit]

    def deleteOrganizationMembership(userId: Long, organizationMembershipId: Long): IO[ZendeskError, Unit]

    def setOrganizationMembershipAsDefault(userId: Long, organizationMembership: OrganizationMembership): IO[ZendeskError, List[OrganizationMembership]]

    def getSearchResults(query: String): IO[ZendeskError, List[SearchResultEntity]]

    def getSearchResults[T <: SearchResultEntity](cls: Class[T], query: String): IO[ZendeskError, List[T]]

    def getSearchResults[T <: SearchResultEntity](cls: Class[T], query: String, params: String): IO[ZendeskError, List[T]]

    def notifyApp(json: String): IO[ZendeskError, Unit]

    def updateInstallation(id: Int, json: String): IO[ZendeskError, Unit]

    def getSatisfactionRatings: IO[ZendeskError, List[SatisfactionRating]]

    def getSatisfactionRating(id: Long): IO[ZendeskError, SatisfactionRating]

    def createSatisfactionRating(ticketId: Long, satisfactionRating: SatisfactionRating): IO[ZendeskError, SatisfactionRating]

    def createSatisfactionRating(ticket: Ticket, satisfactionRating: SatisfactionRating): IO[ZendeskError, SatisfactionRating]

    def getHelpCenterLocales: IO[ZendeskError, List[String]]

    def getArticles: IO[ZendeskError, List[Article]]

    def getArticles(category: Category): IO[ZendeskError, List[Article]]

    def getArticlesIncrementally(startTime: Date): IO[ZendeskError, List[Article]]

    def getArticlesFromPage(page: Int): IO[ZendeskError, List[Article]]

    def getArticle(articleId: Long): IO[ZendeskError, Article]

    def getArticleTranslations(articleId: Long): IO[ZendeskError, List[Translation]]

    def createArticle(article: Article): IO[ZendeskError, Article]

    def updateArticle(article: Article): IO[ZendeskError, Article]

    def createArticleTranslation(articleId: Long, translation: Translation): IO[ZendeskError, Translation]

    def updateArticleTranslation(articleId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation]

    def deleteArticle(article: Article): IO[ZendeskError, Unit]

    def deleteArticleAttachment(attachment: ArticleAttachments): IO[ZendeskError, Unit]

    def deleteArticleAttachment(attachmentId: Long): IO[ZendeskError, Unit]

    def getCategories: IO[ZendeskError, List[Category]]

    def getCategory(categoryId: Long): IO[ZendeskError, Category]

    def getCategoryTranslations(categoryId: Long): IO[ZendeskError, List[Translation]]

    def createCategory(category: Category): IO[ZendeskError, Category]

    def updateCategory(category: Category): IO[ZendeskError, Category]

    def createCategoryTranslation(categoryId: Long, translation: Translation): IO[ZendeskError, Translation]

    def updateCategoryTranslation(categoryId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation]

    def deleteCategory(category: Category): IO[ZendeskError, Unit]

    def getSections: IO[ZendeskError, List[Section]]

    def getSections(category: Category): IO[ZendeskError, List[Section]]

    def getSection(sectionId: Long): IO[ZendeskError, Section]

    def getSectionTranslations(sectionId: Long): IO[ZendeskError, List[Translation]]

    def createSection(section: Section): IO[ZendeskError, Section]

    def updateSection(section: Section): IO[ZendeskError, Section]

    def createSectionTranslation(sectionId: Long, translation: Translation): IO[ZendeskError, Translation]

    def updateSectionTranslation(sectionId: Long, locale: String, translation: Translation): IO[ZendeskError, Translation]

    def deleteSection(section: Section): IO[ZendeskError, Unit]

    def getUserSubscriptions(user: User): IO[ZendeskError, List[Subscription]]

    def getUserSubscriptions(userId: Long): IO[ZendeskError, List[Subscription]]

    def getArticleSubscriptions(articleId: Long): IO[ZendeskError, List[Subscription]]

    def getArticleSubscriptions(articleId: Long, locale: String): IO[ZendeskError, List[Subscription]]

    def getSectionSubscriptions(sectionId: Long): IO[ZendeskError, List[Subscription]]

    def getSectionSubscriptions(sectionId: Long, locale: String): IO[ZendeskError, List[Subscription]]

    def getSchedules: IO[ZendeskError, List[Schedule]]

    def getSchedule(schedule: Schedule): IO[ZendeskError, Schedule]

    def getSchedule(scheduleId: Long): IO[ZendeskError, Schedule]

    def getHolidaysForSchedule(schedule: Schedule): IO[ZendeskError, List[Holiday]]

    def getHolidaysForSchedule(scheduleId: Long): IO[ZendeskError, List[Holiday]]
}