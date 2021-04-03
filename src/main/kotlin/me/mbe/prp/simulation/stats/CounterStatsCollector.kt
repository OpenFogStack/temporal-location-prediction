package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState

class CounterStatsCollector : BaseStatsCollector() {

    private val stats: MutableMap<String, StatsPerUser> = HashMap()

    private class StatsPerUser {
        var noCalled: Int = 0
        var noConnectedToClosestNode: Int = 0
    }

    override fun printStats(simName: String) {
        stats.toSortedMap().forEach { (userName, state) ->
            print("User: $userName; ")
            print("Called: ${state.noCalled}; ")
            print("Correct: ${state.noConnectedToClosestNode}; ")
            print("AccCount: ${state.noConnectedToClosestNode.toDouble() / state.noCalled.toDouble()}; ")
            println()
        }
    }


    override fun onStartTrip(user: User, state: WorldState) {}

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })
        val found = state.isKeygroupMember(state.keyGroups[user.name]!!, closestNode)
        obj.noCalled++
        if (found) {
            obj.noConnectedToClosestNode++
        }
    }

    override fun onEndTrip(user: User, state: WorldState) {}

}