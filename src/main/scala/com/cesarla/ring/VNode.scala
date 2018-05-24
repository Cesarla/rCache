package com.cesarla.ring

import java.util.UUID

case class VNode(id: UUID, start: Long, end: Long) {
  def containsHash(hash: Long): Boolean = start >= hash && hash <= end
}
