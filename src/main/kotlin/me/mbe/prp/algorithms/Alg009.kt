package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

val MaxTimeDiff: Duration = Duration.of(5, ChronoUnit.MINUTES)

class Alg009(user: User) : Algorithm(user) {

    private var lastTime: Instant? = null

    private val trips = LinkedList<LinkedList<Step>>()

    private var currentTrip = LinkedList<Step>()

    override fun doWork(state: WorldState): Instant {
        val correctMembers = LinkedList<Node?>()
        val kg = getKeyGroup(state)
        val closestNode = state.getClosestNode(user)
        correctMembers.add(closestNode)
        state.setKeygroupMembers(kg, correctMembers)

        if (Duration.between(lastTime ?: state.time, state.time) > MaxTimeDiff) {
            trips.add(currentTrip)
            currentTrip.last.end = lastTime!!
            currentTrip = LinkedList()



            if (trips.last.last.node != closestNode) {
                println("jump12")
            }
        }

        if (currentTrip.isEmpty() || currentTrip.last.node != closestNode) {
            if (!currentTrip.isEmpty()) {
                currentTrip.last.end = state.time
            }
            currentTrip.add(Step(closestNode, state.time))

            val cT = currentTrip.map { it.node }

            val q = trips.map { it.map(Step::node) }.filter { it.take(cT.size) == cT }

            println(q)
        }

        lastTime = state.time
        return state.time.plus(SECOND)
    }

    override fun printState() {
        println(trips)
    }

}

data class Step(val node: Node, val start: Instant) {
    lateinit var end: Instant
}

/*
class PrefixTree<T>() {
    val root = PrefixTreeNode<T>()

    fun insert(word: List<T>) {
        var c = root
        c.counter++
        word.forEach {
            c = c.children.getOrPut(it, { PrefixTreeNode() })
            c.counter++
        }
    }
}

class PrefixTreeNode<T>() {
    val children: MutableMap<T, PrefixTreeNode<T>> = HashMap()
    var counter = 0

}
*/
