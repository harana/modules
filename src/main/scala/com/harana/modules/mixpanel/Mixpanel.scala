package com.harana.modules.mixpanel

import org.json.{JSONArray, JSONObject}
import zio.Task
import zio.macros.accessible

import java.util.UUID

@accessible
trait Mixpanel {
    def append(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def delete(id: UUID, modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def event(id: UUID, name: String, properties: Map[String, Object]): Task[JSONObject]

    def groupDelete(groupKey: String, groupId: String, modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupMessage(groupKey: String, groupId: String, actionType: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupRemove(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupSet(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupSetOnce(groupKey: String, groupId: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupUnion(groupKey: String, groupId: String, properties: Map[String, JSONArray], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def groupUnset(groupKey: String, groupId: String, propertyNames: List[String], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def increment(id: UUID, properties: Map[String, Long], modifiers: Map[String, String] = Map()): Task[JSONObject]

    def peopleMessage(id: UUID, actionType: String, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def remove(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def send(messages: List[JSONObject]): Task[Unit]

    def set(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def setOnce(id: UUID, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def trackCharge(id: UUID, amount: Double, properties: Map[String, Object], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def union(id: UUID, properties: Map[String, JSONArray], modifiers: Map[String, Object] = Map()): Task[JSONObject]

    def unset(id: UUID, propertyNames: List[String], modifiers: Map[String, String] = Map()): Task[JSONObject]
}