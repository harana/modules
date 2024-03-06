package com.harana.modules.facebook

import com.facebook.ads.sdk.Campaign.{EnumBidStrategy, EnumObjective, EnumSpecialAdCategory}
import com.facebook.ads.sdk._
import zio.Task
import zio.macros.accessible

@accessible
trait Facebook {

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
                 trackingSpecs: Option[String] = None): Task[Ad]

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
                       upstreamEvents: Map[String, String] = Map()): Task[Campaign]

    def adAccount(adAccountId: String): Task[AdAccount]

    def adActivities(adAccountId: String): Task[List[AdActivity]]

    def adCreatives(adAccountId: String): Task[List[AdCreative]]

    def adCreativesByLabels(adAccountId: String): Task[List[AdCreative]]

    def adImages(adAccountId: String): Task[List[AdImage]]

    def adLabels(adAccountId: String): Task[List[AdLabel]]

    def adPlacePageSets(adAccountId: String): Task[List[AdPlacePageSet]]

    def adPlayables(adAccountId: String): Task[List[PlayableContent]]

    def adRulesHistory(adAccountId: String): Task[List[AdAccountAdRulesHistory]]

    def adRulesLibrary(adAccountId: String): Task[List[AdRule]]

    def ads(adAccountId: String): Task[List[Ad]]

    def adsByLabels(adAccountId: String): Task[List[Ad]]

    def adSets(adAccountId: String): Task[List[AdSet]]

    def adSetsByLabels(adAccountId: String): Task[List[AdSet]]

    def adPixels(adAccountId: String): Task[List[AdsPixel]]

    def adStudies(adAccountId: String): Task[List[AdStudy]]

    def adVolume(adAccountId: String): Task[List[AdAccountAdVolume]]

    def adAdvertisableApplications(adAccountId: String): Task[List[Application]]

    def adAffectedAdSets(adAccountId: String): Task[List[AdSet]]

    def adAgencies(adAccountId: String): Task[List[Business]]

    def adApplications(adAccountId: String): Task[List[Application]]

    def adAssignedUsers(adAccountId: String): Task[List[AssignedUser]]

    def adAsyncRequests(adAccountId: String): Task[List[AsyncRequest]]

    def adCampaigns(adAccountId: String): Task[List[Campaign]]
    
    def adCampaignsByLabels(adAccountId: String): Task[List[Campaign]]

    def adCustomAudiences(adAccountId: String): Task[List[CustomAudience]]

    def adCustomAudiencesTOS(adAccountId: String): Task[List[CustomAudiencesTOS]]

    def adCustomConversions(adAccountId: String): Task[List[CustomConversion]]

    def adDeliveryEstimate(adAccountId: String): Task[List[AdAccountDeliveryEstimate]]

    def adDeprecatedTargetingAdSets(adAccountId: String): Task[List[AdSet]]

    def adImpactingAdStudies(adAccountId: String): Task[List[AdStudy]]

    def adInsights(adAccountId: String): Task[List[AdsInsights]]

    def adInstagramAccounts(adAccountId: String): Task[List[InstagramUser]]

    def adMatchedSearchApplications(adAccountId: String): Task[List[AdAccountMatchedSearchApplicationsEdgeData]]

    def adMaxBid(adAccountId: String): Task[List[AdAccountMaxBid]]

    def adMinimumBudgets(adAccountId: String): Task[List[MinimumBudget]]

    def adOfflineConversionDataSets(adAccountId: String): Task[List[OfflineConversionDataSet]]

    def adOnBehalfRequests(adAccountId: String): Task[List[BusinessOwnedObjectOnBehalfOfRequest]]

    def adPromotePages(adAccountId: String): Task[List[Page]]

    def adPublisherBlockLists(adAccountId: String): Task[List[PublisherBlockList]]

    def adReachEstimate(adAccountId: String): Task[List[AdAccountReachEstimate]]

    def adReachFrequencyPredictions(adAccountId: String): Task[List[ReachFrequencyPrediction]]

    def adSavedAudiences(adAccountId: String): Task[List[SavedAudience]]

    def adSubscribedApps(adAccountId: String): Task[List[AdAccountSubscribedApps]]

    def adTargetingBrowse(adAccountId: String): Task[List[AdAccountTargetingUnified]]

    def adTargetingSearch(adAccountId: String): Task[List[AdAccountTargetingUnified]]

    def adTargetingSentenceLines(adAccountId: String): Task[List[TargetingSentenceLine]]

    def adTargetingSuggestions(adAccountId: String): Task[List[AdAccountTargetingUnified]]

    def adTargetingValidation(adAccountId: String): Task[List[AdAccountTargetingUnified]]

    def adTracking(adAccountId: String): Task[List[AdAccountTrackingData]]

    def adUsers(adAccountId: String): Task[List[AdAccountUser]]

    def userAccounts(userId: String): Task[List[Page]]

    def userAdAccounts(userId: String): Task[List[AdAccount]]

    def userAdStudies(userId: String): Task[List[AdStudy]]

    def userAlbums(userId: String): Task[List[Album]]

    def userAppRequests(userId: String): Task[List[AppRequest]]

    def userAssignedAdAccounts(userId: String): Task[List[AdAccount]]

    def userAssignedBusinessAssetGroups(userId: String): Task[List[BusinessAssetGroup]]

    def userAssignedPages(userId: String): Task[List[Page]]

    def userAssignedProductCatalogs(userId: String): Task[List[ProductCatalog]]

    def userBusinesses(userId: String): Task[List[Business]]

    def userBusinessUsers(userId: String): Task[List[BusinessUser]]

    def userConversations(userId: String): Task[List[UnifiedThread]]

    def userCustomLabels(userId: String): Task[List[PageUserMessageThreadLabel]]

    def userEvents(userId: String): Task[List[Event]]

    def userFriends(userId: String): Task[List[User]]

    def userAdVideos(userId: String): Task[List[AdVideo]]

    def video(videoId: String): Task[AdVideo]

    def videoThumbnails(videoId: String): Task[List[VideoThumbnail]]

}