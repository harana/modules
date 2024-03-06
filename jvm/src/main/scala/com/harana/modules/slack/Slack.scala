package com.harana.modules.slack

import com.hubspot.slack.client.SlackClient
import com.hubspot.slack.client.methods.{ResultSort, ResultSortOrder}
import com.hubspot.slack.client.models.conversations.{Conversation, ConversationType}
import com.hubspot.slack.client.models.dialog.SlackDialog
import com.hubspot.slack.client.models.files.SlackFile
import com.hubspot.slack.client.models.group.SlackGroup
import com.hubspot.slack.client.models.response.auth.AuthTestResponse
import com.hubspot.slack.client.models.response.{MessagePage, SlackError}
import com.hubspot.slack.client.models.teams.SlackTeam
import com.hubspot.slack.client.models.usergroups.SlackUsergroup
import com.hubspot.slack.client.models.users.SlackUser
import com.hubspot.slack.client.models.{Attachment, LiteMessage, SlackChannel}
import zio.IO
import zio.macros.accessible

@accessible
trait Slack {
    def newClient(token: String): IO[Nothing, SlackClient]

    def Auth(client: SlackClient): IO[Either[SlackError, Throwable], AuthTestResponse]

    def revokeAuth(client: SlackClient): IO[Either[SlackError, Throwable], Boolean]

    def searchMessages(client: SlackClient,
                       count: Int,
                       page: Int,
                       query: String,
                       shouldHighlight: Boolean,
                       sort: ResultSort,
                       sortOrder: ResultSortOrder): IO[Either[SlackError, Throwable], MessagePage]

    def findReplies(client: SlackClient,
                    channelId: String,
                    threadTs: String): IO[Either[SlackError, Throwable], List[LiteMessage]]

    def findUser(client: SlackClient,
                 userId: String,
                 includeLocale: Boolean): IO[Either[SlackError, Throwable], SlackUser]

    def lookupUserByEmail(client: SlackClient,
                          email: String): IO[Either[SlackError, Throwable], SlackUser]

    def listUsers(client: SlackClient): IO[Either[SlackError, Throwable], List[SlackUser]]

    def listUsersPaginated(client: SlackClient,
                           cursor: Option[String],
                           limit: Option[Int]): IO[Either[SlackError, Throwable], List[SlackUser]]

    def listChannels(client: SlackClient,
                     cursor: Option[String],
                     limit: Option[Int],
                     shouldExcludeArchived: Boolean,
                     shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], List[SlackChannel]]

    def channelHistory(client: SlackClient,
                       channelId: String,
                       count: Option[Int],
                       inclusive: Boolean): IO[Either[SlackError, Throwable], List[LiteMessage]]

    def getChannelByName(client: SlackClient,
                         channelName: String,
                         shouldExcludeArchived: Boolean,
                         shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], SlackChannel]

    def getChannelInfo(client: SlackClient,
                       channelId: String,
                       includeLocale: Boolean): IO[Either[SlackError, Throwable], SlackChannel]

    def kickUserFromChannel(client: SlackClient,
                            channelId: String,
                            userId: String): IO[Either[SlackError, Throwable], Unit]

    def listGroups(client: SlackClient,
                   shouldExcludeArchived: Boolean,
                   shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], List[SlackGroup]]

    def kickUserFromGroup(client: SlackClient,
                          channelId: String,
                          userId: String): IO[Either[SlackError, Throwable], Unit]

    def openIm(client: SlackClient,
               includeLocale: Boolean,
               returnIm: Boolean,
               userId: String): IO[Either[SlackError, Throwable], Unit]

    def postMessage(client: SlackClient,
                    asUser: Boolean,
                    attachments: List[Attachment],
                    channelId: String,
                    iconEmoji: Option[String],
                    iconUrl: Option[String],
                    replyBroadcast: Boolean,
                    shouldLinkNames: Boolean,
                    text: Option[String],
                    threadTs: Option[String]): IO[Either[SlackError, Throwable], String]

    def postEphemeralMessage(client: SlackClient,
                             attachments: List[Attachment],
                             channelId: String,
                             parseMode: String,
                             sendAsUser: Boolean,
                             shouldLinkNames: Boolean,
                             text: Option[String],
                             threadTs: Option[String],
                             userToSendTo: String): IO[Either[SlackError, Throwable], String]

    def updateMessage(client: SlackClient,
                      asUser: Boolean,
                      attachments: List[Attachment],
                      channelId: String,
                      parse: String,
                      shouldLinkNames: Boolean,
                      text: Option[String],
                      ts: String): IO[Either[SlackError, Throwable], Unit]

    def getPermalink(client: SlackClient,
                     channelId: String,
                     messageTs: String): IO[Either[SlackError, Throwable], String]

    def deleteMessage(client: SlackClient,
                      asUser: Boolean,
                      channelId: String,
                      messageToDeleteTs: String): IO[Either[SlackError, Throwable], Unit]

    def listConversations(client: SlackClient,
                          conversationTypes: List[ConversationType],
                          cursor: Option[String],
                          limit: Option[Int],
                          shouldExcludeArchived: Boolean): IO[Either[SlackError, Throwable], List[Conversation]]

    def usersConversations(client: SlackClient,
                           cursor: Option[String],
                           limit: Option[Int],
                           shouldExcludeArchived: Boolean,
                           userId: Option[String]): IO[Either[SlackError, Throwable], List[Conversation]]

    def createConversation(client: SlackClient,
                           isPrivate: Boolean,
                           name: String): IO[Either[SlackError, Throwable], Unit]

    def inviteToConversation(client: SlackClient,
                             channelId: String,
                             users: List[String]): IO[Either[SlackError, Throwable], Unit]

    def unarchiveConversation(client: SlackClient,
                              channelId: String): IO[Either[SlackError, Throwable], Unit]

    def getConversationHistory(client: SlackClient,
                               channelId: String,
                               inclusive: Boolean,
                               limit: Option[Int],
                               newestTimestamp: Option[String],
                               oldestTimestamp: Option[String]): IO[Either[SlackError, Throwable], List[LiteMessage]]

    def archiveConversation(client: SlackClient,
                            channelId: String): IO[Either[SlackError, Throwable], Unit]

    def getConversationInfo(client: SlackClient,
                            conversationId: String,
                            includeLocale: Boolean): IO[Either[SlackError, Throwable], Conversation]

    def getConversationReplies(client: SlackClient,
                               channel: String,
                               ts: String): IO[Either[SlackError, Throwable], List[LiteMessage]]

    def getConversationByName(client: SlackClient,
                              conversationName: String,
                              conversationTypes: List[ConversationType],
                              shouldExcludeArchived: Boolean): IO[Either[SlackError, Throwable], Conversation]

    def openConversation(client: SlackClient,
                         channelId: Option[String],
                         returnIm: Boolean,
                         users: List[String]): IO[Either[SlackError, Throwable], Conversation]

    def createUsergroup(client: SlackClient,
                        description: Option[String],
                        handle: Option[String],
                        includeCount: Boolean,
                        name: String,
                        rawChannelIds: List[String]): IO[Either[SlackError, Throwable], SlackUsergroup]

    def listUsergroups(client: SlackClient,
                       includeCount: Boolean,
                       includeDisabled: Boolean,
                       includeUsers: Boolean): IO[Either[SlackError, Throwable], List[SlackUsergroup]]

    def updateUsergroup(client: SlackClient,
                        description: Option[String],
                        handle: Option[String],
                        includeCount: Boolean,
                        name: Option[String],
                        rawChannelIds: List[String],
                        userGroupId: String): IO[Either[SlackError, Throwable], SlackUsergroup]

    def enableUsergroup(client: SlackClient,
                        includeCount: Boolean,
                        userGroupId: String): IO[Either[SlackError, Throwable], Unit]

    def disableUsergroup(client: SlackClient,
                         includeCount: Boolean,
                         userGroupId: String): IO[Either[SlackError, Throwable], Unit]

    def updateUsergroupUsers(client: SlackClient,
                             includeCount: Boolean,
                             rawUserIds: List[String],
                             userGroupId: String): IO[Either[SlackError, Throwable], SlackUsergroup]

    def openDialog(client: SlackClient,
                   slackDialog: SlackDialog,
                   triggerId: String): IO[Either[SlackError, Throwable], Unit]

    def addReaction(client: SlackClient,
                    channel: Option[String],
                    file: Option[String],
                    fileComment: Option[String],
                    name: String,
                    timestamp: Option[String]): IO[Either[SlackError, Throwable], Unit]

    def getTeamInfo(client: SlackClient): IO[Either[SlackError, Throwable], SlackTeam]

    def uploadFile(client: SlackClient,
                   channels: List[String],
                   content: Option[String],
                   filename: Option[String],
                   initialComment: Option[String],
                   threadTs: Option[String],
                   title: Option[String]): IO[Either[SlackError, Throwable], SlackFile]

    def shareFilePublically(client: SlackClient,
                            fileId: String): IO[Either[SlackError, Throwable], SlackFile]

    def listEmoji(client: SlackClient): IO[Either[SlackError, Throwable], Map[String, String]]
}