package com.harana.modules.slack

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.hubspot.slack.client.methods.params.auth.AuthRevokeParams
import com.hubspot.slack.client.methods.params.channels._
import com.hubspot.slack.client.methods.params.chat._
import com.hubspot.slack.client.methods.params.conversations._
import com.hubspot.slack.client.methods.params.dialog.DialogOpenParams
import com.hubspot.slack.client.methods.params.files.{FilesSharedPublicUrlParams, FilesUploadParams}
import com.hubspot.slack.client.methods.params.group.{GroupsKickParams, GroupsListParams}
import com.hubspot.slack.client.methods.params.im.ImOpenParams
import com.hubspot.slack.client.methods.params.reactions.ReactionsAddParams
import com.hubspot.slack.client.methods.params.search.SearchMessagesParams
import com.hubspot.slack.client.methods.params.usergroups._
import com.hubspot.slack.client.methods.params.usergroups.users.UsergroupUsersUpdateParams
import com.hubspot.slack.client.methods.params.users.{UserEmailParams, UsersInfoParams, UsersListParams}
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
import com.hubspot.slack.client.{SlackClient, SlackClientFactory, SlackClientRuntimeConfig}
import zio.{IO, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object LiveSlack {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveSlack(config, logger, micrometer)
  }
}

case class LiveSlack(config: Config, logger: Logger, micrometer: Micrometer) extends Slack {

  def newClient(token: String): IO[Nothing, SlackClient] = {
    val config = SlackClientRuntimeConfig.builder()
      .setTokenSupplier(() => token)
      .build()
    ZIO.succeed(SlackClientFactory.defaultFactory().build(config))
  }


  def Auth(client: SlackClient): IO[Either[SlackError, Throwable], AuthTestResponse] =
    client.testAuth()


  def revokeAuth(client: SlackClient): IO[Either[SlackError, Throwable], Boolean] =
    client.revokeAuth(AuthRevokeParams.builder().build()).map(_.isRevoked)


  def searchMessages(client: SlackClient,
                     count: Int,
                     page: Int,
                     query: String,
                     shouldHighlight: Boolean,
                     sort: ResultSort,
                     sortOrder: ResultSortOrder): IO[Either[SlackError, Throwable], MessagePage] =
    client.searchMessages(
      SearchMessagesParams.builder()
        .setCount(count)
        .setPage(page)
        .setQuery(query)
        .setShouldHighlight(shouldHighlight)
        .setSort(sort)
        .setSortOrder(sortOrder)
        .build()
    ).map(_.getMessages)


  def findReplies(client: SlackClient,
                  channelId: String,
                  threadTs: String): IO[Either[SlackError, Throwable], List[LiteMessage]] =
    client.findReplies(
      FindRepliesParams.builder()
        .setChannelId(channelId)
        .setThreadTs(threadTs)
        .build()
    ).map(_.getMessages.asScala.toList)


  def findUser(client: SlackClient,
               userId: String,
               includeLocale: Boolean): IO[Either[SlackError, Throwable], SlackUser] =
    client.findUser(
      UsersInfoParams.builder()
        .setIncludeLocale(includeLocale)
        .setUserId(userId)
        .build()
    ).map(_.getUser)


  def lookupUserByEmail(client: SlackClient,
                        email: String): IO[Either[SlackError, Throwable], SlackUser] =
    client.lookupUserByEmail(
      UserEmailParams.builder()
        .setEmail(email)
        .build()
    ).map(_.getUser)


  def listUsers(client: SlackClient): IO[Either[SlackError, Throwable], List[SlackUser]] =
    client.listUsers()


  def listUsersPaginated(client: SlackClient,
                         cursor: Option[String],
                         limit: Option[Int]): IO[Either[SlackError, Throwable], List[SlackUser]] =
    client.listUsersPaginated(
      UsersListParams.builder()
        .setCursor(cursor)
        .setLimit(limit)
        .build()
    ).map(_.getMembers.asScala.toList)


  def listChannels(client: SlackClient,
                   cursor: Option[String],
                   limit: Option[Int],
                   shouldExcludeArchived: Boolean,
                   shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], List[SlackChannel]] =
    client.listChannels(
      ChannelsListParams.builder()
        .setCursor(cursor)
        .setLimit(limit)
        .setShouldExcludeArchived(shouldExcludeArchived)
        .setShouldExcludeMembers(shouldExcludeMembers)
        .build()
    )


  def channelHistory(client: SlackClient,
                     channelId: String,
                     count: Option[Int],
                     inclusive: Boolean): IO[Either[SlackError, Throwable], List[LiteMessage]] =
    client.channelHistory(
      ChannelsHistoryParams.builder()
        .setChannelId(channelId)
        .setCount(count)
        .setInclusive(inclusive)
        .build()
    )


  def getChannelByName(client: SlackClient,
                       channelName: String,
                       shouldExcludeArchived: Boolean,
                       shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], SlackChannel] =
    client.getChannelByName(
      channelName,
      ChannelsFilter.builder()
        .setShouldExcludeArchived(shouldExcludeArchived)
        .setShouldExcludeMembers(shouldExcludeMembers)
        .build()
    )


  def getChannelInfo(client: SlackClient,
                     channelId: String,
                     includeLocale: Boolean): IO[Either[SlackError, Throwable], SlackChannel] =
    client.getChannelInfo(
      ChannelsInfoParams.builder()
        .setChannelId(channelId)
        .setIncludeLocale(includeLocale)
        .build()
    ).map(_.getChannel)


  def kickUserFromChannel(client: SlackClient,
                          channelId: String,
                          userId: String): IO[Either[SlackError, Throwable], Unit] =
    client.kickUserFromChannel(
      ChannelsKickParams.builder()
        .setChannelId(channelId)
        .setUserId(userId)
        .build()
    ).unit


  def listGroups(client: SlackClient,
                 shouldExcludeArchived: Boolean,
                 shouldExcludeMembers: Boolean): IO[Either[SlackError, Throwable], List[SlackGroup]] =
    client.listGroups(
      GroupsListParams.builder()
        .setShouldExcludeArchived(shouldExcludeArchived)
        .setShouldExcludeMembers(shouldExcludeMembers)
        .build())


  def kickUserFromGroup(client: SlackClient,
                        channelId: String,
                        userId: String): IO[Either[SlackError, Throwable], Unit] =
    client.kickUserFromGroup(
      GroupsKickParams.builder()
        .setChannelId(channelId)
        .setUserId(userId)
        .build()
    ).unit


  def openIm(client: SlackClient,
             includeLocale: Boolean,
             returnIm: Boolean,
             userId: String): IO[Either[SlackError, Throwable], Unit] =
    client.openIm(
      ImOpenParams.builder()
        .setIncludeLocale(includeLocale)
        .setReturnIm(returnIm)
        .setUserId(userId)
        .build()
    ).unit


  def postMessage(client: SlackClient,
                  asUser: Boolean,
                  attachments: List[Attachment],
                  channelId: String,
                  iconEmoji: Option[String],
                  iconUrl: Option[String],
                  replyBroadcast: Boolean,
                  shouldLinkNames: Boolean,
                  text: Option[String],
                  threadTs: Option[String]): IO[Either[SlackError, Throwable], String] =
    client.postMessage(
      ChatPostMessageParams.builder()
        .setAsUser(asUser)
        .setAttachments(attachments.asJava)
        .setChannelId(channelId)
        .setIconEmoji(iconEmoji)
        .setIconUrl(iconUrl)
        .setLinkNames(shouldLinkNames)
        .setReplyBroadcast(replyBroadcast)
        .setText(text)
        .setThreadTs(threadTs)
        .build()
    ).map(_.getTs)


  def postEphemeralMessage(client: SlackClient,
                           attachments: List[Attachment],
                           channelId: String,
                           parseMode: String,
                           sendAsUser: Boolean,
                           shouldLinkNames: Boolean,
                           text: Option[String],
                           threadTs: Option[String],
                           userToSendTo: String): IO[Either[SlackError, Throwable], String] =
    client.postEphemeralMessage(
      ChatPostEphemeralMessageParams.builder()
        .setAttachments(attachments.asJava)
        .setChannelId(channelId)
        .setParseMode(parseMode)
        .setSendAsUser(sendAsUser)
        .setShouldLinkNames(shouldLinkNames)
        .setText(text)
        .setThreadTs(threadTs)
        .setUserToSendTo(userToSendTo)
        .build()
    ).map(_.getMessageTs)


  def updateMessage(client: SlackClient,
                    asUser: Boolean,
                    attachments: List[Attachment],
                    channelId: String,
                    parse: String,
                    shouldLinkNames: Boolean,
                    text: Option[String],
                    ts: String): IO[Either[SlackError, Throwable], Unit] =
    client.updateMessage(
      ChatUpdateMessageParams.builder()
        .setAsUser(asUser)
        .setAttachments(attachments.asJava)
        .setChannelId(channelId)
        .setParse(parse)
        .setShouldLinkNames(shouldLinkNames)
        .setText(text)
        .setTs(ts)
        .build()
    ).unit


  def getPermalink(client: SlackClient,
                   channelId: String,
                   messageTs: String): IO[Either[SlackError, Throwable], String] =
    client.getPermalink(
      ChatGetPermalinkParams.builder()
        .setChannelId(channelId)
        .setMessageTs(messageTs)
        .build()
    ).map(_.getPermalink)


  def deleteMessage(client: SlackClient,
                    asUser: Boolean,
                    channelId: String,
                    messageToDeleteTs: String): IO[Either[SlackError, Throwable], Unit] =
    client.deleteMessage(
      ChatDeleteParams.builder()
        .setAsUser(asUser)
        .setChannelId(channelId)
        .setMessageToDeleteTs(messageToDeleteTs)
        .build()
    ).unit


  def listConversations(client: SlackClient,
                        conversationTypes: List[ConversationType],
                        cursor: Option[String],
                        limit: Option[Int],
                        shouldExcludeArchived: Boolean): IO[Either[SlackError, Throwable], List[Conversation]] =
    client.listConversations(
      ConversationsListParams.builder()
        .setConversationTypes(conversationTypes.asJava)
        .setCursor(cursor)
        .setLimit(limit)
        .setShouldExcludeArchived(shouldExcludeArchived)
        .build())


  def usersConversations(client: SlackClient,
                         cursor: Option[String],
                         limit: Option[Int],
                         shouldExcludeArchived: Boolean,
                         userId: Option[String]): IO[Either[SlackError, Throwable], List[Conversation]] =
    client.usersConversations(
      ConversationsUserParams.builder()
        .setCursor(cursor)
        .setLimit(limit)
        .setShouldExcludeArchived(shouldExcludeArchived)
        .setUserId(userId)
        .build()
    )


  def createConversation(client: SlackClient,
                         isPrivate: Boolean,
                         name: String): IO[Either[SlackError, Throwable], Unit] =
    client.createConversation(
      ConversationCreateParams.builder()
        .setPrivate(isPrivate)
        .setName(name)
        .build()
    ).unit


  def inviteToConversation(client: SlackClient,
                           channelId: String,
                           users: List[String]): IO[Either[SlackError, Throwable], Unit] =
    client.inviteToConversation(
      ConversationInviteParams.builder()
        .setChannelId(channelId)
        .setUsers(users.asJava)
        .build()
    ).unit


  def unarchiveConversation(client: SlackClient, channelId: String): IO[Either[SlackError, Throwable], Unit] =
    client.unarchiveConversation(
      ConversationUnarchiveParams.builder()
        .setChannelId(channelId)
        .build()
    ).unit


  def getConversationHistory(client: SlackClient,
                             channelId: String,
                             inclusive: Boolean,
                             limit: Option[Int],
                             newestTimestamp: Option[String],
                             oldestTimestamp: Option[String]): IO[Either[SlackError, Throwable], List[LiteMessage]] =
    client.getConversationHistory(
      ConversationsHistoryParams.builder()
        .setChannelId(channelId)
        .setInclusive(inclusive)
        .setLimit(limit)
        .setNewestTimestamp(newestTimestamp)
        .setOldestTimestamp(oldestTimestamp)
        .build()
    )


  def archiveConversation(client: SlackClient,
                          channelId: String): IO[Either[SlackError, Throwable], Unit] =
    client.archiveConversation(
      ConversationArchiveParams.builder()
        .setChannelId(channelId)
        .build()
    ).unit


  def getConversationInfo(client: SlackClient,
                          conversationId: String,
                          includeLocale: Boolean): IO[Either[SlackError, Throwable], Conversation] =
    client.getConversationInfo(
      ConversationsInfoParams.builder()
        .setConversationId(conversationId)
        .setIncludeLocale(includeLocale)
        .build()
    ).map(_.getConversation)


  def getConversationReplies(client: SlackClient,
                             channel: String,
                             ts: String): IO[Either[SlackError, Throwable], List[LiteMessage]] =
    client.getConversationReplies(
      ConversationsRepliesParams.builder()
        .setChannel(channel)
        .setTs(ts)
        .build()
    ).map(_.getMessages.asScala.toList)


  def getConversationByName(client: SlackClient,
                            conversationName: String,
                            conversationTypes: List[ConversationType],
                            shouldExcludeArchived: Boolean): IO[Either[SlackError, Throwable], Conversation] =
    client.getConversationByName(
      conversationName,
      ConversationsFilter.builder()
        .setConversationTypes(conversationTypes.asJava)
        .setShouldExcludeArchived(shouldExcludeArchived)
        .build()
    ).map(c => Conversation.builder().setId(c.getId).setName(c.getName).build())


  def openConversation(client: SlackClient,
                       channelId: Option[String],
                       returnIm: Boolean,
                       users: List[String]): IO[Either[SlackError, Throwable], Conversation] =
    client.openConversation(
      ConversationOpenParams.builder()
        .setChannelId(channelId)
        .setReturnIm(returnIm)
        .setUsers(users.asJava)
        .build()
    ).map(_.getConversation)


  // Usergroups
  def createUsergroup(client: SlackClient,
                      description: Option[String],
                      handle: Option[String],
                      includeCount: Boolean,
                      name: String,
                      rawChannelIds: List[String]): IO[Either[SlackError, Throwable], SlackUsergroup] =
    client.createUsergroup(
      UsergroupCreateParams.builder()
        .setDescription(description)
        .setHandle(handle)
        .setIncludeCount(includeCount)
        .setName(name)
        .setRawChannelIds(rawChannelIds.asJava)
        .build()
    ).map(_.getUsergroup)


  def listUsergroups(client: SlackClient,
                     includeCount: Boolean,
                     includeDisabled: Boolean,
                     includeUsers: Boolean): IO[Either[SlackError, Throwable], List[SlackUsergroup]] =
    client.listUsergroups(
      UsergroupListParams.builder()
        .setIncludeCount(includeCount)
        .setIncludeDisabled(includeDisabled)
        .setIncludeUsers(includeUsers)
        .build()
    )


  def updateUsergroup(client: SlackClient,
                      description: Option[String],
                      handle: Option[String],
                      includeCount: Boolean,
                      name: Option[String],
                      rawChannelIds: List[String],
                      userGroupId: String): IO[Either[SlackError, Throwable], SlackUsergroup] =
    client.updateUsergroup(
      UsergroupUpdateParams.builder()
        .setDescription(description)
        .setHandle(handle)
        .setIncludeCount(includeCount)
        .setName(name)
        .setRawChannelIds(rawChannelIds.asJava)
        .setUsergroupId(userGroupId)
        .build()
    ).map(_.getUsergroup)


  def enableUsergroup(client: SlackClient,
                      includeCount: Boolean,
                      userGroupId: String): IO[Either[SlackError, Throwable], Unit] = {
    client.enableUsergroup(
      UsergroupEnableParams.builder()
        .setIncludeCount(includeCount)
        .setUsergroupId(userGroupId)
        .build()
    ).unit
  }


  def disableUsergroup(client: SlackClient,
                       includeCount: Boolean,
                       userGroupId: String): IO[Either[SlackError, Throwable], Unit] =
    client.disableUsergroup(
      UsergroupDisableParams.builder()
        .setIncludeCount(includeCount)
        .setUsergroupId(userGroupId)
        .build()
    ).unit


  def updateUsergroupUsers(client: SlackClient,
                           includeCount: Boolean,
                           rawUserIds: List[String],
                           userGroupId: String): IO[Either[SlackError, Throwable], SlackUsergroup] =
    client.updateUsergroupUsers(
      UsergroupUsersUpdateParams.builder()
        .setIncludeCount(includeCount)
        .setRawUserIds(rawUserIds.asJava)
        .setUsergroupId(userGroupId)
        .build()
    ).map(_.getUsergroup)


  def openDialog(client: SlackClient,
                 slackDialog: SlackDialog,
                 triggerId: String): IO[Either[SlackError, Throwable], Unit] =
    client.openDialog(
      DialogOpenParams.builder()
        .setDialog(slackDialog)
        .setTriggerId(triggerId)
        .build()
    ).unit


  def addReaction(client: SlackClient,
                  channel: Option[String],
                  file: Option[String],
                  fileComment: Option[String],
                  name: String,
                  timestamp: Option[String]): IO[Either[SlackError, Throwable], Unit] =
    client.addReaction(
      ReactionsAddParams.builder()
        .setChannel(channel)
        .setFile(file)
        .setFileComment(fileComment)
        .setName(name)
        .setTimestamp(timestamp)
        .build()
    ).unit


  def getTeamInfo(client: SlackClient): IO[Either[SlackError, Throwable], SlackTeam] =
    client.getTeamInfo.map(_.getSlackTeam)


  def uploadFile(client: SlackClient,
                 channels: List[String],
                 content: Option[String],
                 filename: Option[String],
                 initialComment: Option[String],
                 threadTs: Option[String],
                 title: Option[String]): IO[Either[SlackError, Throwable], SlackFile] =
    client.uploadFile(
      FilesUploadParams.builder()
        .setChannels(channels.asJava)
        .setContent(content)
        .setFilename(filename)
        .setInitialComment(initialComment)
        .setThreadTs(threadTs)
        .setTitle(title)
        .build()
    ).map(_.getFile)


  def shareFilePublically(client: SlackClient,
                          fileId: String): IO[Either[SlackError, Throwable], SlackFile] =
    client.shareFilePublically(
      FilesSharedPublicUrlParams.builder()
        .setFileId(fileId)
        .build()
    ).map(_.getFile)


  def listEmoji(client: SlackClient): IO[Either[SlackError, Throwable], Map[String, String]] =
    client.listEmoji.map(_.getEmoji.asScala.toMap)
}