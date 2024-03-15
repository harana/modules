package com.harana.modules.segment

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.segment.models.SegmentOptions
import com.segment.analytics.Analytics
import com.segment.analytics.messages._
import zio.{IO, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object LiveSegment {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveSegment(config, logger, micrometer)
  }
}

case class LiveSegment(config: Config, logger: Logger, micrometer: Micrometer) extends Segment {

  private val analytics = config.secret("segment-write-key").map(c => Analytics.builder(c).build)

  def alias(previousId: String, userId: String, options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = AliasMessage.builder(previousId)
    sendMessage(builder, userId, options)
  }

  def group(userId: String, groupId: String, traits: Map[String, _], options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = GroupMessage.builder(groupId).traits(traits.asJava)
    sendMessage(builder, userId, options)
  }

  def identify(userId: String, traits: Map[String, _], options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = IdentifyMessage.builder().traits(traits.asJava)
    sendMessage(builder, userId, options)
  }

  def page(userId: String, name: String, properties: Map[String, _], options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = PageMessage.builder(name).properties(properties.asJava)
    sendMessage(builder, userId, options)
  }

  def screen(userId: String, name: String, properties: Map[String, _], options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = ScreenMessage.builder(name).properties(properties.asJava)
    sendMessage(builder, userId, options)
  }

  def track(userId: String, event: String, properties: Map[String, _], options: SegmentOptions): IO[Nothing, Unit] = {
    val builder = TrackMessage.builder(event).properties(properties.asJava)
    sendMessage(builder, userId, options)
  }

  def flush: IO[Nothing, Unit] =
    analytics.map(_.flush())

  private def sendMessage(builder: MessageBuilder[_ <: Message, _ <: MessageBuilder[_ <: Message, _ <: AnyRef]],
                          userId: String,
                          options: SegmentOptions) = {
    if (options.isAnonymous) builder.anonymousId(userId) else builder.userId(userId)
    if (options.timestamp.nonEmpty) builder.timestamp(options.timestamp.get)
    //if (options.integrationOptions.nonEmpty) builder.integrationOptions(options.integrationOptions.get._1, options.integrationOptions.get._2.asJava)
    builder.context(options.context.asJava)
    options.enabledIntegrations.foreach { i => builder.enableIntegration(i._1, i._2) }
    analytics.map(_.enqueue(builder))
  }
}