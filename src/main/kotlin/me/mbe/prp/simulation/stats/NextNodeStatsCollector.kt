package me.mbe.prp.simulation.stats

import com.floern.castingcsv.typeadapter.CsvTypeAdapter
import me.mbe.prp.base.DateTimeAdapter
import me.mbe.prp.base.writeCsv
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.time.OffsetDateTime
import java.util.LinkedList

class NextNodeStatsCollector : BaseStatsCollector() {

    private val stats: MutableMap<String, StatsPerUser> = LinkedHashMap()


    private class StatsPerUser {
        var lastNode: Node? = null
        var noNext: Int = 0
        var noNextCorrect: Int = 0
        val overTime: LinkedList<StatsOverTime> = LinkedList()
    }

    data class StatsOverTime(
        @CsvTypeAdapter(DateTimeAdapter::class) val time: Instant,
        val noNext: Int,
        val noNextCorrect: Int,

        val nodeID: String,
    )

    override fun printStats(simName: String) {
        val statsOverall = StatsPerUser()
        stats.toSortedMap().forEach { (userName, state) ->
            statsOverall.noNext += state.noNext
            statsOverall.noNextCorrect += state.noNextCorrect
            print("User: $userName; ")
            print("NoNext: ${state.noNext}; ")
            print("NoNextCorrect: ${state.noNextCorrect}; ")
            print("AccNext: ${state.noNextCorrect.toDouble() / state.noNext.toDouble()}; ")
            println()
            // writeCsv(state.overTime, "./stats-out/$simName/next-node/$userName.csv")
        }
        print("User: overall; ")
        print("NoNext: ${statsOverall.noNext}; ")
        print("NoNextCorrect: ${statsOverall.noNextCorrect}; ")
        print("AccNext: ${statsOverall.noNextCorrect.toDouble() / statsOverall.noNext.toDouble()}; ")
        println()
    }

    override fun collect(user: User, state: WorldState, closestNode: Node) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })
        val found = state.isKeygroupMember(state.keyGroups[user.name]!!, closestNode)
        if (obj.lastNode != null && obj.lastNode != closestNode) {
            obj.noNext++
            if (found) {
                obj.noNextCorrect++
            }
            obj.overTime.addLast(StatsOverTime(state.time, obj.noNext, obj.noNextCorrect, closestNode.name))
        }
        obj.lastNode = closestNode
    }

}