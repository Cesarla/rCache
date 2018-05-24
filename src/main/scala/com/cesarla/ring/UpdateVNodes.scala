package com.cesarla.ring

import akka.cluster.Member

case class UpdateVNodes(vNodes: Map[VNode, Member])
