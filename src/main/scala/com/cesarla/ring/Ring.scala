package com.cesarla.ring

import java.util.UUID

import scala.annotation.tailrec

case class Ring(vNodes: Map[VNode, Node] = Map.empty[VNode, Node]) {

  def nodes: List[Node] = vNodes.values.toSet.toList

  def addNode(node: Node): Ring = {
    if (containsNode(node)) this else Ring(reshuffle(nodes :+ node)(vNodes))
  }

  def removeNode(node: Node): Ring = {
    if (containsNode(node)) Ring(reshuffle(nodes.filterNot(_ == node))(vNodes)) else this
  }

  def containsNode(node: Node): Boolean = vNodes.values.exists(_ == node)

  private[this] def reshuffle(nodes: List[Node])(vNodes: Map[VNode, Node]): Map[VNode, Node] = {
    val (target, source) = calculateDiff(nodes)(vNodes)
    val newVNodes = redistributeVNodes(vNodes)(target, source)
    newVNodes
  }

  @tailrec
  private[this] final def redistributeVNodes(vNodes: Map[VNode, Node])(source: Seq[(Node, Seq[VNode])],
                                                                       target: Seq[(Node, Int)]): Map[VNode, Node] = {
    target match {
      case (targetMember, amountRequired) :: tTail =>
        source match {
          case (sourceMember, extraVNodes) :: sTail if extraVNodes.length - amountRequired >= 0 =>
            val (transferredVNodes, remainingVNodes) = extraVNodes.splitAt(amountRequired)
            val newVNodes = transferredVNodes.foldLeft(vNodes)((ac, vn) => ac + (vn -> targetMember))
            redistributeVNodes(newVNodes)((sourceMember, remainingVNodes) :: sTail, tTail)
          case (_, extraVNodes) :: sTail =>
            val newVNodes = extraVNodes.foldLeft(vNodes)((ac, vn) => ac + (vn -> targetMember))
            redistributeVNodes(newVNodes)(sTail, (targetMember, amountRequired - extraVNodes.length) :: tTail)
          case Nil => vNodes
        }
      case Nil => vNodes
    }
  }

  private[this] def calculateDiff(nodes: List[Node])(
      vNodes: Map[VNode, Node]): (List[(Node, List[VNode])], List[(Node, Int)]) = {
    val vNodesByMember = vNodes.groupBy(_._2).mapValues(_.keys.toList)
    @tailrec
    def go(_nodes: List[Node])(extra: Int)(
        sourceVNodes: List[(Node, List[VNode])],
        targetVNodes: List[(Node, Int)]): (List[(Node, List[VNode])], List[(Node, Int)]) = {
      _nodes match {
        case member :: tail =>
          val size = vNodesByMember.get(member).map(_.length).getOrElse(0)
          val desiredSize = { if (extra > 0) 1 else 0 } + Ring.VNodesNumber / nodes.size
          if (size > desiredSize) {
            val vNodes = vNodesByMember.getOrElse(member, List.empty[VNode])
            go(tail)(extra - 1)(sourceVNodes :+ ((member, vNodes.take(size - desiredSize))), targetVNodes)
          } else if (size < desiredSize) {
            go(tail)(extra - 1)(sourceVNodes, targetVNodes :+ ((member, Math.abs(size - desiredSize))))
          } else {
            go(tail)(extra - 1)(sourceVNodes, targetVNodes)
          }
        case Nil => (sourceVNodes, targetVNodes)
      }
    }

    val mod = Ring.VNodesNumber % nodes.size
    go(nodes.sortBy(_.address))(mod)(List.empty[(Node, List[VNode])], List.empty[(Node, Int)])
  }

}

object Ring {

  val VNodesNumber = 64

  def initialize(node: Node): Ring = Ring(initializeVNodes(VNodesNumber).map(_ -> node).toMap)

  private[this] def initializeVNodes(vNodes: Int): Seq[VNode] = {
    val hashRangeSize: Long = vNodeHashRangeSize(vNodes)
    val mod = ((-BigInt(Long.MinValue) + BigInt(Long.MaxValue)) % vNodes).toInt

    @tailrec
    def go(minHash: Long, remaining: Int, prev: Seq[VNode] = Seq.empty[VNode]): Seq[VNode] = {
      if (minHash + hashRangeSize == Long.MaxValue) {
        prev :+ VNode(UUID.randomUUID(), minHash, minHash + hashRangeSize)
      } else if (minHash < Long.MaxValue) {
        go(minHash + hashRangeSize + { if (remaining > 0) 1 else 0 },
           remaining - 1,
           prev :+ VNode(UUID.randomUUID(), minHash, minHash + hashRangeSize))
      } else prev
    }

    go(Long.MinValue, mod)
  }

  private[this] def vNodesPerNode(vNodes: Int, nodes: Int): Int = vNodes / nodes

  private[this] def vNodeHashRangeSize(vNodes: Int = 56): Long = {
    ((-BigInt(Long.MinValue) + BigInt(Long.MaxValue)) / vNodes).toLong
  }
}
