package me.mbe.prp.nodes

import me.mbe.prp.core.Location
import me.mbe.prp.core.Node
import me.mbe.prp.core.NodeWithLatency
import me.mbe.prp.core.NodesGetter
import me.mbe.prp.simulation.helpers.LatencyFunction
import me.mbe.prp.simulation.helpers.simpleLatencyCalculation

class SimpleNodesGetter(
    override val nodes: List<Node>,
    private val latencyFunction: LatencyFunction = ::simpleLatencyCalculation,
) : NodesGetter() {

    override fun getClosestNodeInternal(p: Location): Node {
        return getNodesWithLatency(p).minByOrNull { it.latency }!!.node
    }

    fun getKClosestNodes(p: Location, k: Int): List<NodeWithLatency> {
        return getNodesWithLatency(p).sortedBy { it.latency }.take(k)
    }

    private fun getNodesWithLatency(p: Location): List<NodeWithLatency> {
        return nodes.map { node -> NodeWithLatency(node, latencyFunction(node.location.distance(p))) }
    }
}