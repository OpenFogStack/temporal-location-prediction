package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.LinkedList
import kotlin.collections.ArrayList

open class Alg003(
    user: User,
    private val noLastNodes: Int = 2,
    topN: Int = 1,
    private val backupTransitionTable: TransitionTable<List<Node>, Node>? = null,
) : Algorithm(user) {
    private var lastNodes: LinkedList<Node> = LinkedList()

    protected open val transitionTable = TransitionTable<List<Node>, Node>(topN)

    override fun doWork(state: WorldState): Instant {
        val currentNode = state.getClosestNode(user)
        val kg = getKeyGroup(state)

        if (lastNodes.isEmpty() || currentNode != lastNodes.last()) {
            val correctMembers = LinkedList<Node>()
            correctMembers.add(currentNode)

            if (lastNodes.size == noLastNodes) {
                transitionTable.addTransition(ArrayList(lastNodes) /* shallow copy */, currentNode)
                backupTransitionTable?.addTransition(ArrayList(lastNodes) /* shallow copy */, currentNode)
            }

            lastNodes.addLast(currentNode)
            while (lastNodes.size > noLastNodes) { //should be only 0 or 1 loops; just to be save
                lastNodes.removeFirst()
            }

            if (lastNodes.size == noLastNodes) {
                val nextNodes = transitionTable.getNext(lastNodes)
                if (nextNodes != null) {
                    correctMembers.addAll(nextNodes)
                } else {
                    backupTransitionTable?.getNext(lastNodes)?.let { correctMembers.addAll(it) }
                }
            }

            state.setKeygroupMembers(kg, correctMembers)
        }

        return state.time.plus(SECOND)
    }

    override fun printState() {
        println(transitionTable)
    }
}