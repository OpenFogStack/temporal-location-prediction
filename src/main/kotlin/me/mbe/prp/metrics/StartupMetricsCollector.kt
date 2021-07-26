package me.mbe.prp.metrics

import me.mbe.prp.core.*
import java.util.*

class StartupMetricsCollector() : BaseMetricsCollector() {

    private val metrics: MutableMap<String, MetricsPerUser> = LinkedHashMap()

    private class MetricsPerUser {
        var noStarts: Int = 0
        var noStartsCorrect: Int = 0
    }

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        val metricsOverall = MetricsPerUser()
        metrics.toSortedMap().forEach { (userName, state) ->
            metricsOverall.noStarts += state.noStarts
            metricsOverall.noStartsCorrect += state.noStartsCorrect
            if (printUserStats) {
                print("User: $userName; ")
                print("NoStarts: ${state.noStarts}; ")
                print("NoStartsCorrect: ${state.noStartsCorrect}; ")
                print("Accuracy@Start: ${state.noStartsCorrect.toDouble() / state.noStarts.toDouble()}; ")
                println()
            }
        }

        val accStart = metricsOverall.noStartsCorrect.toDouble() / metricsOverall.noStarts.toDouble()

        print("User: overall; ")
        print("NoStarts: ${metricsOverall.noStarts}; ")
        print("NoStartsCorrect: ${metricsOverall.noStartsCorrect}; ")
        print("Accuracy@Start: ${accStart}; ")
        println()

        writeValue(accStart, simName)
    }

    override fun loadAndPrintStats(simName: String) {
        println("Accuracy@Start: ${loadValue(simName)};")
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {}

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = metrics.getOrPut(user.name, { MetricsPerUser() })
        val found = state.isKeygroupMember(state.getKeygroup(user.name), state.getClosestNode(user))
        obj.noStarts++
        if (found) {
            obj.noStartsCorrect++
        }
    }

    override fun onEndTrip(user: User, state: WorldState) {}

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {}
}