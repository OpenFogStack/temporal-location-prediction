package me.mbe.prp.algorithms

import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User

open class Alg004(user: User, sim: Simulation, noLastNodes: Int = 2, topN: Int = 1) : Alg003(user, sim, noLastNodes) {
    override val transitionTable = SmartTransitionTable<Node>(noLastNodes, topN)

    override fun printState() {
        super.printState()
        transitionTable.printStats()
    }
}

class SmartTransitionTable<T>(val maxLength: Int, topN: Int) : TransitionTable<List<T>, T>(topN) {

    private val getNextCalled = LinkedHashMap<Int, Int>()

    override fun getNext(current: List<T>): List<T>? {
        getNextCalled.merge(maxLength, 1, Int::plus)
        var s = super.getNext(current)
        var i = maxLength - 1
        while ((s == null || s.isEmpty()) && i > 0) {
            s = getNextSmart(current, i)
            i--
        }
        if (s == null) {
            getNextCalled.merge(0, 1, Int::plus)
        }
        return s
    }

    //checks for suffix match of "length"; aggregates all; returns max
    private fun getNextSmart(current: List<T>, length: Int): List<T>? {
        getNextCalled.merge(length, 1, Int::plus)
        val cSuffix = current.takeLast(length)
        val v = map.entries.filter { it.key.takeLast(length) == cSuffix }

        //aggregate
        val newMap = LinkedHashMap<T, Int>()
        v.forEach { hashMap ->
            hashMap.value.entries.forEach { entry -> newMap.merge(entry.key, entry.value, Int::plus) }
        }

        //returns max
        //return newMap.entries.maxByOrNull { it.value }?.key
        return newMap.entries.sortedByDescending { it.value }.map { it.key }.take(topN)
    }

    fun printStats() {
        println(getNextCalled)
    }


}
