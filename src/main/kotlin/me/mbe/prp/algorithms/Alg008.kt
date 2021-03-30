package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.helpers.GridNodeGetter
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.util.*


class Alg008(user: User) : Algorithm(user) {

    override fun doWork(state: WorldState): Instant {
        val correctMembers = LinkedList<Node?>()
        val kg = getKeyGroup(state)
        val closestNode = state.getClosestNode(user)
        correctMembers.add(closestNode)

        val nodes = state.nodes as GridNodeGetter

        val possibleNextNodes = nodes.grid.neighbors(closestNode)

        val nextNode = possibleNextNodes
            .map { Pair(it, it.location.distance(user.location)) }
            .minByOrNull { it.second }

        correctMembers.add(nextNode?.first)

        state.setKeygroupMembers(kg, correctMembers)
        return state.time.plus(SECOND)
    }

}

