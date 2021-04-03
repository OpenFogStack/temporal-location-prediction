package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.util.*


//could be replaced by external knowledge on neighboring nodes: see Alg008
val transitionTable = TransitionTable<Node, Node>(topN = Int.MAX_VALUE)


class Alg007(user: User, sim: Simulation) : Algorithm(user, sim) {

    var lastNode: Node? = null

    override fun onStartTrip(state: WorldState) {}

    override fun onNewPosition(state: WorldState) {
        val correctMembers = LinkedList<Node?>()
        val kg = getKeyGroup(state)
        val closestNode = state.getClosestNode(user)
        correctMembers.add(closestNode)

        if (lastNode != null && lastNode!! != closestNode) {
            transitionTable.addTransition(lastNode!!, closestNode)
        }

        val possibleNextNodes = transitionTable.getNext(closestNode)

        val nextNode = possibleNextNodes?.map {
            Pair(it, it.location.distance(user.location))
        }?.minByOrNull { it.second }

        correctMembers.add(nextNode?.first)

        lastNode = closestNode
        state.setKeygroupMembers(kg, correctMembers)
    }

    override fun onEndTrip(state: WorldState) {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf())
    }

}

