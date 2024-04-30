package com.harana.modules.facebook

import com.facebook.ads.sdk.Campaign.{EnumBidStrategy, EnumObjective, EnumSpecialAdCategory}
import com.facebook.ads.sdk._
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.ZIO.attemptBlocking
import zio.{Task, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object LiveFacebook {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveFacebook(config, logger, micrometer)
  }
}

case class LiveFacebook(config: Config, logger: Logger, micrometer: Micrometer) extends Facebook {

    private val apiContext = for {
      accessToken <- config.secret("facebook-access-token")
      appSecret   <- config.secret("facebook-app-secret")
    } yield {
      new APIContext(accessToken, appSecret)
    }

    def createAd(adAccountId: String,
                 adLabels: Option[String] = None,
                 adsetId: Option[String] = None,
                 adsetSpec: Option[AdSet] = None,
                 audienceId: Option[String] = None,
                 bidAmount: Option[Long] = None,
                 creative: Option[AdCreative] = None,
                 dateFormat: Option[String] = None,
                 displaySequence: Option[Long] = None,
                 draftAdgroupId: Option[String] = None,
                 engagementAudience: Option[Boolean] = None,
                 executionOptions: List[Ad.EnumExecutionOptions] = List(),
                 includeDemoLinkHashes: Option[Boolean] = None,
                 name: Option[String] = None,
                 priority: Option[Long] = None,
                 sourceAdId: Option[String] = None,
                 status: Option[Ad.EnumStatus] = None,
                 trackingSpecs: Option[String] = None): Task[Ad] =
      for {
        ac <- apiContext
        ad <- ZIO.attempt {
          var ad = new AdAccount(adAccountId, ac).createAd()
          if (adLabels.nonEmpty) ad.setAdlabels(adLabels.get)
          if (adsetId.nonEmpty) ad.setAdsetId(adsetId.get)
          if (adsetSpec.nonEmpty) ad.setAdsetSpec(adsetSpec.get)
          if (audienceId.nonEmpty) ad.setAudienceId(audienceId.get)
          if (bidAmount.nonEmpty) ad.setBidAmount(bidAmount.get)
          if (creative.nonEmpty) ad.setCreative(creative.get)
          if (dateFormat.nonEmpty) ad.setDateFormat(dateFormat.get)
          if (displaySequence.nonEmpty) ad.setDisplaySequence(displaySequence.get)
          if (draftAdgroupId.nonEmpty) ad.setDraftAdgroupId(draftAdgroupId.get)
          if (engagementAudience.nonEmpty) ad.setEngagementAudience(engagementAudience.get)
          ad.setExecutionOptions(executionOptions.asJava)
          if (includeDemoLinkHashes.nonEmpty) ad.setIncludeDemolinkHashes(includeDemoLinkHashes.get)
          if (name.nonEmpty) ad.setName(name.get)
          if (priority.nonEmpty) ad.setPriority(priority.get)
          if (sourceAdId.nonEmpty) ad.setSourceAdId(sourceAdId.get)
          if (status.nonEmpty) ad.setStatus(status.get)
          if (trackingSpecs.nonEmpty) ad.setTrackingSpecs(trackingSpecs.get)

          ad.execute()
        }
      } yield ad

    def createCampaign(adAccountId: String,
                       adLabels: Option[String] = None,
                       bidStrategy: Option[EnumBidStrategy] = None,
                       budgetRebalanceFlag: Option[Boolean] = None,
                       buyingType: Option[String] = None,
                       dailyBudget: Option[Long] = None,
                       executionOptions: List[Campaign.EnumExecutionOptions] = List(),
                       iterativeSplitTestConfigs: Option[String] = None,
                       lifetimeBudget: Option[Long] = None,
                       name: Option[String] = None,
                       objective: Option[EnumObjective] = None,
                       pacingTypes: List[String] = List(),
                       promotedObject: Option[String] = None,
                       sourceCampaignId: Option[String] = None,
                       specialAdCategory: Option[EnumSpecialAdCategory] = None,
                       spendCap: Option[Long] = None,
                       status: Option[Campaign.EnumStatus] = None,
                       topLineId: Option[String] = None,
                       upstreamEvents: Map[String, String] = Map()): Task[Campaign] =
      for {
        ac <- apiContext
        campaign <- ZIO.attempt {
          var campaign = new AdAccount(adAccountId, ac).createCampaign()
          if (adLabels.nonEmpty) campaign = campaign.setAdlabels(adLabels.get)
          if (bidStrategy.nonEmpty) campaign = campaign.setBidStrategy(bidStrategy.get)
          if (buyingType.nonEmpty) campaign.setBuyingType(buyingType.get)
          if (dailyBudget.nonEmpty) campaign.setDailyBudget(dailyBudget.get)
          campaign.setExecutionOptions(executionOptions.asJava)
          if (iterativeSplitTestConfigs.nonEmpty) campaign.setIterativeSplitTestConfigs(iterativeSplitTestConfigs.get)
          if (lifetimeBudget.nonEmpty) campaign.setLifetimeBudget(lifetimeBudget.get)
          if (name.nonEmpty) campaign.setName(name.get)
          if (objective.nonEmpty) campaign.setObjective(objective.get)
          campaign.setPacingType(pacingTypes.asJava)
          if (promotedObject.nonEmpty) campaign.setPromotedObject(promotedObject.get)
          if (sourceCampaignId.nonEmpty) campaign.setSourceCampaignId(sourceCampaignId.get)
          if (spendCap.nonEmpty) campaign.setSpendCap(spendCap.get)
          if (status.nonEmpty) campaign.setStatus(status.get)
          if (topLineId.nonEmpty) campaign.setToplineId(topLineId.get)
          campaign.execute()
        }
      } yield campaign

    def adAccount(adAccountId: String): Task[AdAccount] =
      for {
        ac <- apiContext
        pages <- ZIO.attempt(new AdAccount(adAccountId, ac).get.requestAllFields.execute())
      } yield pages

    def adActivities(adAccountId: String): Task[List[AdActivity]] =
      adAccount(adAccountId, _.getActivities.requestAllFields.execute())

    def adCreatives(adAccountId: String): Task[List[AdCreative]] =
      adAccount(adAccountId, _.getAdCreatives.requestAllFields.execute())

    def adCreativesByLabels(adAccountId: String): Task[List[AdCreative]] =
      adAccount(adAccountId, _.getAdCreativesByLabels.requestAllFields.execute())

    def adImages(adAccountId: String): Task[List[AdImage]] =
      adAccount(adAccountId, _.getAdImages.requestAllFields.execute())

    def adLabels(adAccountId: String): Task[List[AdLabel]] =
      adAccount(adAccountId, _.getAdLabels.requestAllFields.execute())

    def adPlacePageSets(adAccountId: String): Task[List[AdPlacePageSet]] =
      adAccount(adAccountId, _.getAdPlacePageSets.requestAllFields.execute())

    def adPlayables(adAccountId: String): Task[List[PlayableContent]] =
      adAccount(adAccountId, _.getAdPlayables.requestAllFields.execute())

    def adRulesHistory(adAccountId: String): Task[List[AdAccountAdRulesHistory]] =
      adAccount(adAccountId, _.getAdRulesHistory.requestAllFields.execute())

    def adRulesLibrary(adAccountId: String): Task[List[AdRule]] =
      adAccount(adAccountId, _.getAdRulesLibrary.requestAllFields.execute())

    def ads(adAccountId: String): Task[List[Ad]] =
      adAccount(adAccountId, _.getAds.requestAllFields.execute())

    def adsByLabels(adAccountId: String): Task[List[Ad]] =
      adAccount(adAccountId, _.getAdsByLabels.requestAllFields.execute())

    def adSets(adAccountId: String): Task[List[AdSet]] =
      adAccount(adAccountId, _.getAdSets.requestAllFields.execute())

    def adSetsByLabels(adAccountId: String): Task[List[AdSet]] =
      adAccount(adAccountId, _.getAdSetsByLabels.requestAllFields.execute())

    def adPixels(adAccountId: String): Task[List[AdsPixel]] =
      adAccount(adAccountId, _.getAdsPixels.requestAllFields.execute())

    def adStudies(adAccountId: String): Task[List[AdStudy]] =
      adAccount(adAccountId, _.getAdStudies.requestAllFields.execute())

    def adVolume(adAccountId: String): Task[List[AdAccountAdVolume]] =
      adAccount(adAccountId, _.getAdsVolume.requestAllFields.execute())

    def adAdvertisableApplications(adAccountId: String): Task[List[Application]] =
      adAccount(adAccountId, _.getAdvertisableApplications.requestAllFields.execute())

    def adAffectedAdSets(adAccountId: String): Task[List[AdSet]] =
      adAccount(adAccountId, _.getAffectedAdSets.requestAllFields.execute())

    def adAgencies(adAccountId: String): Task[List[Business]] =
      adAccount(adAccountId, _.getAgencies.requestAllFields.execute())

    def adApplications(adAccountId: String): Task[List[Application]] =
      adAccount(adAccountId, _.getApplications.requestAllFields.execute())

    def adAssignedUsers(adAccountId: String): Task[List[AssignedUser]] =
      adAccount(adAccountId, _.getAssignedUsers.requestAllFields.execute())

    def adAsyncRequests(adAccountId: String): Task[List[AsyncRequest]] =
      adAccount(adAccountId, _.getAsyncRequests.requestAllFields.execute())

    def adCampaigns(adAccountId: String): Task[List[Campaign]] =
      adAccount(adAccountId, _.getCampaigns.requestAllFields.execute())

    def adCampaignsByLabels(adAccountId: String): Task[List[Campaign]] =
      adAccount(adAccountId, _.getCampaignsByLabels.requestAllFields.execute())

    def adCustomAudiences(adAccountId: String): Task[List[CustomAudience]] =
      adAccount(adAccountId, _.getCustomAudiences.requestAllFields.execute())

    def adCustomAudiencesTOS(adAccountId: String): Task[List[CustomAudiencesTOS]] =
      adAccount(adAccountId, _.getCustomAudiencesTos.requestAllFields.execute())

    def adCustomConversions(adAccountId: String): Task[List[CustomConversion]] =
      adAccount(adAccountId, _.getCustomConversions.requestAllFields.execute())

    def adDeliveryEstimate(adAccountId: String): Task[List[AdAccountDeliveryEstimate]] =
      adAccount(adAccountId, _.getDeliveryEstimate.requestAllFields.execute())

    def adDeprecatedTargetingAdSets(adAccountId: String): Task[List[AdSet]] =
      adAccount(adAccountId, _.getDeprecatedTargetingAdSets.requestAllFields.execute())

    def adImpactingAdStudies(adAccountId: String): Task[List[AdStudy]] =
      adAccount(adAccountId, _.getImpactingAdStudies.requestAllFields.execute())

    def adInsights(adAccountId: String): Task[List[AdsInsights]] =
      adAccount(adAccountId, _.getInsights.requestAllFields.execute())

    def adInstagramAccounts(adAccountId: String): Task[List[InstagramUser]] =
      adAccount(adAccountId, _.getInstagramAccounts.requestAllFields.execute())

    def adMatchedSearchApplications(adAccountId: String): Task[List[AdAccountMatchedSearchApplicationsEdgeData]] =
      adAccount(adAccountId, _.getMatchedSearchApplications.requestAllFields.execute())

    def adMaxBid(adAccountId: String): Task[List[AdAccountMaxBid]] =
      adAccount(adAccountId, _.getMaxBid.requestAllFields.execute())

    def adMinimumBudgets(adAccountId: String): Task[List[MinimumBudget]] =
      adAccount(adAccountId, _.getMinimumBudgets.requestAllFields.execute())

    def adOfflineConversionDataSets(adAccountId: String): Task[List[OfflineConversionDataSet]] =
      adAccount(adAccountId, _.getOfflineConversionDataSets.requestAllFields.execute())

    def adOnBehalfRequests(adAccountId: String): Task[List[BusinessOwnedObjectOnBehalfOfRequest]] =
      adAccount(adAccountId, _.getOnBehalfRequests.requestAllFields.execute())

    def adPromotePages(adAccountId: String): Task[List[Page]] =
      adAccount(adAccountId, _.getPromotePages.requestAllFields.execute())

    def adPublisherBlockLists(adAccountId: String): Task[List[PublisherBlockList]] =
      adAccount(adAccountId, _.getPublisherBlockLists.requestAllFields.execute())

    def adReachEstimate(adAccountId: String): Task[List[AdAccountReachEstimate]] =
      adAccount(adAccountId, _.getReachEstimate.requestAllFields.execute())

    def adReachFrequencyPredictions(adAccountId: String): Task[List[ReachFrequencyPrediction]] =
      adAccount(adAccountId, _.getReachFrequencyPredictions.requestAllFields.execute())

    def adSavedAudiences(adAccountId: String): Task[List[SavedAudience]] =
      adAccount(adAccountId, _.getSavedAudiences.requestAllFields.execute())

    def adSubscribedApps(adAccountId: String): Task[List[AdAccountSubscribedApps]] =
      adAccount(adAccountId, _.getSubscribedApps.requestAllFields.execute())

    def adTargetingBrowse(adAccountId: String): Task[List[AdAccountTargetingUnified]] =
      adAccount(adAccountId, _.getTargetingBrowse.requestAllFields.execute())

    def adTargetingSearch(adAccountId: String): Task[List[AdAccountTargetingUnified]] =
      adAccount(adAccountId, _.getTargetingSearch.requestAllFields.execute())

    def adTargetingSentenceLines(adAccountId: String): Task[List[TargetingSentenceLine]] =
      adAccount(adAccountId, _.getTargetingSentenceLines.requestAllFields.execute())

    def adTargetingSuggestions(adAccountId: String): Task[List[AdAccountTargetingUnified]] =
      adAccount(adAccountId, _.getTargetingSuggestions.requestAllFields.execute())

    def adTargetingValidation(adAccountId: String): Task[List[AdAccountTargetingUnified]] =
      adAccount(adAccountId, _.getTargetingValidation.requestAllFields.execute())

    def adTracking(adAccountId: String): Task[List[AdAccountTrackingData]] =
      adAccount(adAccountId, _.getTracking.requestAllFields.execute())

    def adUsers(adAccountId: String): Task[List[AdAccountUser]] =
      adAccount(adAccountId, _.getUsers.requestAllFields.execute())

    def userAccounts(userId: String): Task[List[Page]] =
      user(userId, _.getAccounts.requestAllFields.execute())

    def userAdAccounts(userId: String): Task[List[AdAccount]] =
      user(userId, _.getAdAccounts.requestAllFields.execute())

    def userAdStudies(userId: String): Task[List[AdStudy]] =
      user(userId, _.getAdStudies.requestAllFields.execute())

    def userAlbums(userId: String): Task[List[Album]] =
      user(userId, _.getAlbums.requestAllFields.execute())

    def userAppRequests(userId: String): Task[List[AppRequest]] =
      user(userId, _.getAppRequests.requestAllFields.execute())

    def userAssignedAdAccounts(userId: String): Task[List[AdAccount]] =
      user(userId, _.getAssignedAdAccounts.requestAllFields.execute())

    def userAssignedBusinessAssetGroups(userId: String): Task[List[BusinessAssetGroup]] =
      user(userId, _.getAssignedBusinessAssetGroups.requestAllFields.execute())

    def userAssignedPages(userId: String): Task[List[Page]] =
      user(userId, _.getAssignedPages.requestAllFields.execute())

    def userAssignedProductCatalogs(userId: String): Task[List[ProductCatalog]] =
      user(userId, _.getAssignedProductCatalogs.requestAllFields.execute())

    def userBusinesses(userId: String): Task[List[Business]] =
      user(userId, _.getBusinesses.requestAllFields.execute())

    def userBusinessUsers(userId: String): Task[List[BusinessUser]] =
      user(userId, _.getBusinessUsers.requestAllFields.execute())

    def userConversations(userId: String): Task[List[UnifiedThread]] =
      user(userId, _.getConversations.requestAllFields.execute())

    def userCustomLabels(userId: String): Task[List[PageUserMessageThreadLabel]] =
      user(userId, _.getCustomLabels.requestAllFields.execute())

    def userEvents(userId: String): Task[List[Event]] =
      user(userId, _.getEvents.requestAllFields.execute())

    def userFriends(userId: String): Task[List[User]] =
      user(userId, _.getFriends.requestAllFields.execute())

    def userAdVideos(userId: String): Task[List[AdVideo]] =
      user(userId, _.getVideos.requestAllFields.execute())

    def video(videoId: String): Task[AdVideo] =
      for {
        ac <- apiContext
        video <- ZIO.attempt(new AdVideo(videoId, ac).get().requestAllFields.execute())
      } yield video

    def videoThumbnails(videoId: String): Task[List[VideoThumbnail]] =
      for {
        ac <- apiContext
        thumbnails <- ZIO.attempt(new AdVideo(videoId, ac).getThumbnails.requestAllFields.execute())
      } yield thumbnails


    private def adAccount[A <: APINode](adAccountId: String, fn: AdAccount => APINodeList[A]): Task[List[A]] =
      for {
        ac <- apiContext
        list <- attemptBlocking(fn(new AdAccount(adAccountId, ac)))
      } yield list

    private def user[A <: APINode](userId: String, fn: User => APINodeList[A]): Task[List[A]] =
      for {
        ac <- apiContext
        list <- attemptBlocking(fn(new User(userId, ac)))
      } yield list

    private implicit def toList[A <: APINode](nodeList: APINodeList[A]): List[A] =
      nodeList.iterator().asScala.toList
}
