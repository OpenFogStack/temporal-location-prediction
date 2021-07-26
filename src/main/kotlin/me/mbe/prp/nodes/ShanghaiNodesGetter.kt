package me.mbe.prp.nodes

import me.mbe.prp.core.Location
import me.mbe.prp.core.Node
import me.mbe.prp.core.NodesGetter

class ShanghaiNodesGetter(
    override val nodes: List<Node>,
) : NodesGetter() {

    private val nodeMap: Map<Location, Node> = nodes.associateBy { it.location }

    override fun getClosestNodeInternal(p: Location): Node {
        return nodeMap[p]!!
    }
}