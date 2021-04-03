package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.KeygroupMember
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.max


class TimeStatsCollector : BaseStatsCollector() {
    companion object {
        init {
            //for loading DateTimeFormatter class before it is needed in collect; in profile it distorts the results
            DateTimeFormatter.BASIC_ISO_DATE
        }
    }

    private val stats: MutableMap<String, StatsPerUser> = LinkedHashMap()


    private class StatsPerUser {
        var lastCollect: Instant? = null
        var lastKeygroupMember: KeygroupMember? = null


        var overAllTime: Long = 0
        var correctTime: Long = 0
    }

    override fun printStats(simName: String) {
        val statsOverall = StatsPerUser()
        stats.toSortedMap().forEach { (userName, state) ->
            statsOverall.overAllTime += state.overAllTime
            statsOverall.correctTime += state.correctTime
            print("User: $userName; ")
            print("AccTime: ${state.correctTime.toDouble() / state.overAllTime.toDouble()}; ")
            println()
        }
        print("User: overall; ")
        print("AccTime: ${statsOverall.correctTime.toDouble() / statsOverall.overAllTime.toDouble()}; ")
        println()
    }

    fun collect(user: User, state: WorldState, closestNode: Node?) {
        val userName = user.name
        val obj = stats[userName]!!

        if (obj.lastKeygroupMember != null) {
            val timeDiff = Duration.between(obj.lastCollect, state.time)
            obj.overAllTime += timeDiff.seconds
            obj.correctTime += listOf(obj.lastKeygroupMember!!)
                    .filter { it.availableFrom.isBefore(state.time) }
                    .map { max(obj.lastCollect!!.epochSecond, it.availableFrom.epochSecond) }
                    .map { state.time.epochSecond - it }
                    .sum()
        }

        obj.lastCollect = state.time
        if (closestNode != null) {
            obj.lastKeygroupMember = state.keyGroups[user.name]!!.members[closestNode.name]
        } else {
            obj.lastKeygroupMember = null
        }
    }

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })
        obj.lastCollect = state.time
        collect(user, state, null)
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        collect(user, state, closestNode)
    }

    override fun onEndTrip(user: User, state: WorldState) {
        collect(user, state, null)
    }

    override fun onTime(user: User, state: WorldState) {
        collect(user, state, stats[user.name]!!.lastKeygroupMember?.node /* does not change*/)
    }


}