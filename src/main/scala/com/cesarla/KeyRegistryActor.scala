package com.cesarla

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.StatusCodes
import com.cesarla.models._

object KeyRegistryActor {
  final case class GetKeyValue(id: OpId, key: Key)
  final case class SetKeyValue(id: OpId, key: Key, value: Value)
  final case class DeleteKeyValue(id: OpId, key: Key)

  def props: Props = Props[KeyRegistryActor]
}

class KeyRegistryActor extends Actor with ActorLogging {
  import KeyRegistryActor._

  var keyValues = Map.empty[Key, KeyValue]

  def receive: Receive = {
    case GetKeyValue(_, key) =>
      val keyValue = keyValues.get(key)
      if (keyValue.isEmpty) sender() ! OperationFailed("get", key, s"key /$key not present", StatusCodes.NotFound)
      else sender() ! OperationPerformed("get", keyValues.get(key))
    case SetKeyValue(_, key, value) =>
      val oldKeyValue = keyValues.get(key)
      val newKeyValue = KeyValue(key, value)
      keyValues += key -> newKeyValue
      sender() ! OperationPerformed("set", Some(newKeyValue), oldKeyValue)
    case DeleteKeyValue(_, key) =>
      val oldKeyValue = keyValues.get(key)
      if (oldKeyValue.isEmpty) sender() ! OperationFailed("delete", key, s"key /$key not present", StatusCodes.NotFound)
      else {
        keyValues -= key
        sender() ! OperationPerformed("delete", None, oldKeyValue)
      }
  }
}
