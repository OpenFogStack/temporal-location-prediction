package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val WEEK: Duration = Duration.of(7 * 24, ChronoUnit.HOURS)

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
        print(
            "AccTime: ${
                statsOverall.correctTime.toDouble() / statsOverall.overAllTime.toDouble()
            }; "
        )
        println()
    }

    override fun collect(user: User, state: WorldState, closestNode: Node) {
        val userName = user.name
        val obj = stats.getOrPut(userName, { StatsPerUser() })

        val found = state.isKeygroupMember(state.keyGroups[userName]!!, closestNode)

        if (obj.lastCollect != null) {
            val timeDiff = Duration.between(obj.lastCollect, state.time)
            if (timeDiff > WEEK) {
                println("${user.name}, ${obj.lastCollect}, ${state.time}, $timeDiff")
            } else {
                obj.overAllTime += timeDiff.seconds
                if (found) {
                    obj.correctTime += timeDiff.seconds
                }
            }
        }

        obj.lastCollect = state.time
    }

}