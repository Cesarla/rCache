package com.cesarla.ring
import akka.actor.{Actor, ActorLogging, ActorSelection, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, _}
import akka.cluster.{Cluster, Member, MemberStatus}

class MembershipListener(id: Id) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  val selfNode = Node(id, cluster.selfAddress)

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
    case MemberUp(member)         => register(member)
    case MemberRemoved(member, _) => getMemberActor(member) ! NodeRemoved(selfNode)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberJoined(member) =>
      log.info("Member joined: {}", member.address)
  }

  private[this] def getMemberActor(member: Member): ActorSelection = {
    context.actorSelection(RootActorPath(member.address) / "user" / "memberhipListener")
  }

  private[this] def register(member: Member): Unit = {
    getMemberActor(member) ! RegisterNode(selfNode)
  }

}

object MembershipListener {
  def props(id: String): Props = Props(classOf[MembershipListener], id)
}
