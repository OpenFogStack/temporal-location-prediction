package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState

interface StatsCollector {
    fun printStats(simName: String)
    fun collect(user: User, state: WorldState)
}

class CombinedStatsCollector(private vararg val collectors: StatsCollector) : StatsCollector {

    override fun collect(user: User, state: WorldState) {
        // val closestNode by lazy { state.getClosestNode(user) }
        val closestNode = state.getClosestNode(user)
        collectors.iterator().forEach {
            when (it) {
                is BaseStatsCollector -> it.collect(user, state, closestNode)
                else -> it.collect(user, state)
            }
        }
    }
/*
    override fun collect(user: User, state: WorldState) {
        collectors.iterator().forEach { it.collect(user, state) }
    }
*/

    override fun printStats(simName: String) {
        collectors.iterator().forEach { it.printStats(simName) }
    }
}

abstract class BaseStatsCollector : StatsCollector {

    abstract fun collect(user: User, state: WorldState, closestNode: Node)

    final override fun collect(user: User, state: WorldState) {
        val node = state.getClosestNode(user)
        collect(user, state, node)
    }
}


