package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.util.*
import kotlin.collections.LinkedHashMap

class Alg002(user: User, topN: Int = 1) : Algorithm(user) {
    private var oldNode: Node? = null

    private val transitionTable = TransitionTable<Node, Node>(topN)

    override fun doWork(state: WorldState): Instant {
        val currentNode = state.getClosestNode(user)
        val kg = getKeyGroup(state)
        if (currentNode != oldNode) {
            val correctMembers = LinkedList<Node>()
            correctMembers.add(currentNode)
            if (oldNode != null) {
                transitionTable.addTransition(oldNode!!, currentNode)
                val nextNodes = transitionTable.getNext(currentNode)
                if (nextNodes != null) {
                    correctMembers.addAll(nextNodes)
                }
            }
            state.setKeygroupMembers(kg, correctMembers)
            oldNode = currentNode
        }
        return state.time.plus(SECOND)
    }

    override fun printState() {
        println(transitionTable)
    }
}

open class TransitionTable<K, L>(val topN: Int = 1) {
    protected val map = LinkedHashMap<K, LinkedHashMap<L, Int>>()

    fun addTransition(from: K, to: L) {
        val f = map.getOrPut(from, ::LinkedHashMap)
        f[to] = f.getOrDefault(to, 0).inc()
    }

    open fun getNext(current: K): List<L>? {
        //return map[current]?.entries?.maxByOrNull { it.value }?.key
        return map[current]?.entries?.sortedByDescending { it.value }?.map { it.key }?.take(topN)
    }

    override fun toString(): String {
        return map.toString()
    }
}