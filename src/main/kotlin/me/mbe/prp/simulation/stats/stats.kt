package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState

interface StatsCollector {
    val name: String

    fun onStartTrip(user: User, state: WorldState)
    fun onNewPosition(user: User, state: WorldState)
    fun onEndTrip(user: User, state: WorldState)

    fun printStats(simName: String)
    fun onTime(user: User, state: WorldState) {}
}

class CombinedStatsCollector(private vararg val collectors: StatsCollector) : StatsCollector {
    override val name: String
        get() = "CombinedStatsCollector${collectors.map(StatsCollector::name)}"

    override fun onStartTrip(user: User, state: WorldState) {
        collectors.iterator().forEach { it.onStartTrip(user, state) }
    }

    override fun onNewPosition(user: User, state: WorldState) {
        val closestNode = state.getClosestNode(user)
        collectors.iterator().forEach {
            when (it) {
                is BaseStatsCollector -> it.onNewPosition(user, state, closestNode)
                else -> it.onNewPosition(user, state)
            }
        }
    }

    override fun onEndTrip(user: User, state: WorldState) {
        collectors.iterator().forEach { it.onEndTrip(user, state) }
    }

    override fun printStats(simName: String) {
        collectors.iterator().forEach {
            println(it.name)
            it.printStats(simName)
        }
    }

    override fun onTime(user: User, state: WorldState) {
        collectors.iterator().forEach { it.onTime(user, state) }
    }
}

abstract class BaseStatsCollector : StatsCollector {

    override val name: String = this.javaClass.simpleName

    abstract fun onNewPosition(user: User, state: WorldState, closestNode: Node)

    final override fun onNewPosition(user: User, state: WorldState) {
        val node = state.getClosestNode(user)
        onNewPosition(user, state, node)
    }
}


