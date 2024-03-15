package com.harana.modules.mixpanel


import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.mixpanel.mixpanelapi.{ClientDelivery, MessageBuilder, MixpanelAPI}
import org.json.{JSONArray, JSONObject}
import zio.{ZIO, ZLayer}

import java.util.UUID
import scala.jdk.CollectionConverters._

object LiveMixpanel {
  val layer = ZLayer {
    for {
      config        <- ZIO.service[Config]
      logger        <- ZIO.service[Logger]
      micrometer    <- ZIO.service[Micrometer]
    } yield LiveMixpanel(config, logger, micrometer)
  }
}

case class LiveMixpanel(config: Config, logger: Logger, micrometer: Micrometer) extends Mixpanel {

  private val api = new MixpanelAPI()
  private val messageBuilder = config.secret("mixpanel-token").map(t => new MessageBuilder(t))

  def append(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.append(id.toString, toJson(properties), toJson(modifiers)))
    } yield r


  def delete(id: UUID, modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.delete(id.toString, toJson(modifiers)))
    } yield r


  def event(id: UUID, name: String, properties: Map[String, Object]) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.event(id.toString, name, toJson(properties)))
    } yield r


  def groupDelete(groupKey: String, groupId: String, modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupDelete(groupKey, groupId, toJson(modifiers)))
    } yield r


  def groupMessage(groupKey: String, groupId: String, actionType: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupMessage(groupKey, groupId, actionType, toJson(properties), toJson(modifiers)))
    } yield r


  def groupRemove(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupRemove(groupKey, groupId, toJson(properties), toJson(modifiers)))
    } yield r


  def groupSet(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupSet(groupKey, groupId, toJson(properties), toJson(modifiers)))
    } yield r


  def groupSetOnce(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupSetOnce(groupKey, groupId, toJson(properties), toJson(modifiers)))
    } yield r


  def groupUnion(groupKey: String, groupId: String, properties: Map[String, JSONArray], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupUnion(groupKey, groupId, properties.asJava, toJson(modifiers)))
    } yield r


  def groupUnset(groupKey: String, groupId: String, propertyNames: List[String], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.groupUnset(groupKey, groupId, propertyNames.asJava, toJson(modifiers)))
    } yield r


  def increment(id: UUID, properties: Map[String, Long], modifiers: Map[String, String] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.increment(id.toString, properties.view.mapValues(Long.box).toMap.asJava, toJson(modifiers)))
    } yield r


  def peopleMessage(id: UUID, actionType: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.peopleMessage(id.toString, actionType, toJson(properties), toJson(modifiers)))
    } yield r


  def remove(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.remove(id.toString, toJson(properties), toJson(modifiers)))
    } yield r


  def send(messages: List[JSONObject]) = {
    val delivery = new ClientDelivery()
    messages.foreach(delivery.addMessage)
    ZIO.from(api.deliver(delivery)).unit
  }

  def set(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.set(id.toString, toJson(properties), toJson(modifiers)))
    } yield r


  def setOnce(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.setOnce(id.toString, toJson(properties), toJson(modifiers)))
    } yield r


  def trackCharge(id: UUID, amount: Double, properties: Map[String, Object], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.trackCharge(id.toString, amount, toJson(properties), toJson(modifiers)))
    } yield r


  def union(id: UUID, properties: Map[String, JSONArray], modifiers: Map[String, Object] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.union(id.toString, properties.asJava, toJson(modifiers)))
    } yield r


  def unset(id: UUID, propertyNames: List[String], modifiers: Map[String, String] = Map()) =
    for {
      mb <- messageBuilder
      r <- ZIO.attempt(mb.unset(id.toString, propertyNames.asJava, toJson(modifiers)))
    } yield r


  private def toJson(properties: Map[String, Object]) = {
    val json = new JSONObject
    properties.foreach(p => json.put(p._1, p._2))
    json
  }
}