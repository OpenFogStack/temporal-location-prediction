package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.util.*


//could be replaced by external knowledge on neighboring nodes
val transitionTable = TransitionTable<Node, Node>(topN = Int.MAX_VALUE)


class Alg007(user: User) : Algorithm(user) {

    var lastNode: Node? = null

    override fun doWork(state: WorldState): Instant {
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
        return state.time.plus(SECOND)
    }

}

