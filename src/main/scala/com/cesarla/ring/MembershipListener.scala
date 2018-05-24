package com.cesarla.ring

import akka.actor.{Actor, ActorLogging, ActorSelection, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, _}
import akka.cluster.{Cluster, Member, MemberStatus}

class MembershipListener(id: Id) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  val selfNode = Node(id, cluster.selfAddress)

  var ring: Ring = Ring.initialize(selfNode)

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    receiveClusterState orElse receiveNodeMembership
  }

  private[this] def receiveClusterState: Receive = {
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).filterNot(ring.nodes.contains).foreach(register)
      log.info("Current members: {}", ring.nodes.mkString(", "))
    case MemberUp(member)         => register(member)
    case MemberRemoved(member, _) => getMemberActor(member) ! RemovedNode(selfNode)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberJoined(member) =>
      log.info("Member joined: {}", member.address)
  }

  private[this] def receiveNodeMembership: Receive = {
    case RegisterNode(node) if !ring.containsNode(node) =>
      log.info(s"Node $node registered")
      context.watch(sender())
      this.ring = ring.addNode(node)
      log.info(s"\nNode: $node\nRing:\n${ring.vNodes.groupBy(_._2).mapValues(_.size).mkString("\n")}")
    //log.info(s"\nnode: $node\nRing:\n${ring.vNodes.mkString("\n")}")
    case RegisterNode(node) if node != selfNode =>
      log.info(s"Node $node already registered")
      log.info(s"\nNode: $node\nRing:\n${ring.vNodes.groupBy(_._2).mapValues(_.size).mkString("\n")}")
    //log.info(s"\nnode: $node\nRing:\n${ring.vNodes.mkString("\n")}")
    case RemovedNode(node) =>
      log.info(s"Member $node de-registered")
      this.ring = ring.removeNode(node)
      log.info(s"\nNode: $node\nRing:\n${ring.vNodes.groupBy(_._2).mapValues(_.size).mkString("\n")}")
      //log.info(s"\nnode: $node\nRing:\n${ring.vNodes.mkString("\n")}")
      context.unwatch(sender())
      ring.nodes.filterNot(_ == selfNode).map(n => getNodeActor(n) ! RemovedNode(node))
      ()
  }

  private[this] def getMemberActor(member: Member): ActorSelection = {
    context.actorSelection(RootActorPath(member.address) / "user" / "memberhipListener")
  }

  private[this] def getNodeActor(node: Node): ActorSelection = {
    context.actorSelection(RootActorPath(node.address) / "user" / "memberhipListener")
  }
  private[this] def register(member: Member): Unit = {
    ring.nodes.filterNot(_.address == member.address).map(node => getMemberActor(member) ! RegisterNode(node))
    ()
  }
}

object MembershipListener {
  def props(id: String): Props = Props(classOf[MembershipListener], id)
}
