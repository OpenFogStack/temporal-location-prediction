package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.KeygroupMember
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import kotlin.collections.HashMap
import kotlin.math.max


class WrongNodeTimeStatsCollector : BaseStatsCollector() {
    private val stats: MutableMap<String, StatsPerUser> = HashMap()

    private class StatsPerUser {
        var lastCollect: Instant? = null
        var lastKeygroupMembers: List<KeygroupMember>? = null
        var lastClosestNode: Node? = null

        var overAllTime: Long = 0
        var wrongTime: Long = 0
    }


    override fun onStartTrip(user: User, state: WorldState) {
        collect(user, state, null)
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        collect(user, state, closestNode)
    }

    override fun onEndTrip(user: User, state: WorldState) {
        collect(user, state, null)
    }

    override fun onTime(user: User, state: WorldState) {
        collect(user, state, stats[user.name]!!.lastClosestNode /* does not change*/)
    }

    override fun printStats(simName: String) {
        val statsOverall = StatsPerUser()
        stats.toSortedMap().forEach { (userName, state) ->
            statsOverall.overAllTime += state.overAllTime
            statsOverall.wrongTime += state.wrongTime
            print("User: $userName; ")
            print("AccWrongTime: ${state.wrongTime.toDouble() / state.overAllTime.toDouble()}; ")
            println()
        }
        print("User: overall; ")
        print("AccWrongTime: ${statsOverall.wrongTime.toDouble() / statsOverall.overAllTime.toDouble()}; ")
        println()
    }

    fun collect(user: User, state: WorldState, closestNode: Node?) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })

        if (obj.lastCollect != null) {
            val timeDiff = Duration.between(obj.lastCollect, state.time)

            obj.overAllTime += timeDiff.seconds
            obj.wrongTime += obj.lastKeygroupMembers!!
                .asSequence()
                .filter { it.node != obj.lastClosestNode }
                .filter { it.availableFrom.isBefore(state.time) }
                .map { max(obj.lastCollect!!.epochSecond, it.availableFrom.epochSecond) }
                .map { state.time.epochSecond - it }
                .sum()
        }

        val currentMembers = state.keyGroups[user.name]!!.members

        obj.lastCollect = state.time
        obj.lastKeygroupMembers = ArrayList(currentMembers.values) /*shallow copy*/
        obj.lastClosestNode = closestNode
    }

}