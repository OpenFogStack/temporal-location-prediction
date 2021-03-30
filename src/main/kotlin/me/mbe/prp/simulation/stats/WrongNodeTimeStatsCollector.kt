package me.mbe.prp.simulation.stats

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

        var overAllTime: Long = 0
        var wrongTime: Long = 0
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

    override fun collect(user: User, state: WorldState, closestNode: Node) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })

        val currentMembers = state.keyGroups[user.name]!!.members

        if (obj.lastCollect != null) {
            val timeDiff = Duration.between(obj.lastCollect, state.time)
            if (timeDiff > WEEK) {
                println("${user.name}, ${obj.lastCollect}, ${state.time}, $timeDiff")
            } else {
                obj.overAllTime += timeDiff.seconds
                obj.wrongTime += currentMembers.values
                    .asSequence()
                    .filter { it.node != closestNode }
                    .filter { it.availableFrom.isBefore(state.time) }
                    .map { max(obj.lastCollect!!.epochSecond, it.availableFrom.epochSecond) }
                    .map { state.time.epochSecond - it }.sum()

                //.fold(Duration.ZERO) { acc, duration -> acc + duration }
            }
        }

        obj.lastCollect = state.time
    }

}