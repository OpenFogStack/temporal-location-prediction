package me.mbe.prp.metrics

import me.mbe.prp.core.*
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

open class NextNodeMetricsCollector : BaseMetricsCollector() {

    private val metrics: MutableMap<String, MetricsPerUser> = LinkedHashMap()

    private class MetricsPerUser {
        var lastNode: Node? = null
        var noNext: Int = 0
        var noNextCorrect: Int = 0

        val overTime: ArrayList<StatsOverTime> = ArrayList()
    }

    data class StatsOverTime(
        val time: Instant,
        val noNext: Int,
        val noNextCorrect: Int,

        val nodeID: String,
    )

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        val metricsOverall = MetricsPerUser()
        metrics.toSortedMap().forEach { (userName, state) ->
            metricsOverall.noNext += state.noNext
            metricsOverall.noNextCorrect += state.noNextCorrect

            if (printUserStats) {
                print("User: $userName; ")
                print("NoNext: ${state.noNext}; ")
                print("NoNextCorrect: ${state.noNextCorrect}; ")

                print("Accuracy@Move: ${state.noNextCorrect.toDouble() / state.noNext.toDouble()}; ")
                println()
            }
            // writeCsv(state.overTime, "./stats-out/$simName/next-node/$userName.csv")
        }

        val accNext = metricsOverall.noNextCorrect.toDouble() / metricsOverall.noNext.toDouble()

        print("User: overall; ")
        print("NoNext: ${metricsOverall.noNext}; ")
        print("NoNextCorrect: ${metricsOverall.noNextCorrect}; ")

        print("Accuracy@Move: ${accNext}; ")
        println()

        writeValue(accNext, simName)

    }

    override fun loadAndPrintStats(simName: String) {
        println("Accuracy@Move: ${loadValue(simName)};")
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        val obj = metrics[user.name]!!
        val found = state.isKeygroupMember(state.getKeygroup(user.name), closestNode)
        if (obj.lastNode != null && obj.lastNode != closestNode) {
            obj.noNext++
            if (found) obj.noNextCorrect++
            obj.overTime.add(StatsOverTime(state.time, obj.noNext, obj.noNextCorrect, closestNode.name))
        }
        obj.lastNode = closestNode
    }

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = metrics.getOrPut(user.name, { MetricsPerUser() })
        obj.lastNode = null
    }

    override fun onEndTrip(user: User, state: WorldState) {}

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {}

}