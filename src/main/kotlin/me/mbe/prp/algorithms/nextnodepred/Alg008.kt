package me.mbe.prp.algorithms.nextnodepred

import me.mbe.prp.core.*
import me.mbe.prp.nodes.GridNodeGetter
import java.util.*


class Alg008(p: AlgorithmParams) : Algorithm(p) {
    override fun onNewPosition(state: WorldState) {
        val correctMembers = LinkedList<Node>()
        val kg = getKeyGroup(state)
        val closestNode = state.getClosestNode(p.user)
        correctMembers.add(closestNode)

        val nodes = state.nodes as GridNodeGetter

        val possibleNextNodes = nodes.grid.neighbors(closestNode)

        val nextNode = possibleNextNodes
            .map { Pair(it, it.location.distance(p.user.location)) }
            .minByOrNull { it.second }

        if (nextNode != null) correctMembers.add(nextNode.first)

        state.setKeygroupMembers(kg, correctMembers)
    }

    override fun computeSize(): Capacity {
        return ZERO_BYTE
    }
}

