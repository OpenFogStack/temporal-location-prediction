package me.mbe.prp.metrics

import me.mbe.prp.core.*
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class PauseTimeMetricsCollector : BaseMetricsCollector() {

    private val metrics: MutableMap<String, MetricsPerUser> = LinkedHashMap()

    private class MetricsPerUser {
        var lastStop: Instant? = null

        val pauses: ArrayList<Duration> = ArrayList()
    }


    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {}

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = metrics.getOrPut(user.name, ::MetricsPerUser)
        if (obj.lastStop != null) {
            obj.pauses.add(Duration.between(obj.lastStop, state.time))
        }
    }

    override fun onEndTrip(user: User, state: WorldState) {
        metrics[user.name]!!.lastStop = state.time
    }

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {}

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        val aggregate = metrics.flatMap { it.value.pauses }.map { it.seconds.toString() }

        val file = File("./stats-out/$simName/PauseTimeOverall.txt")
        file.parentFile.mkdirs()

        val out = file.outputStream().bufferedWriter()
        aggregate.forEach {
            out.write(it)
            out.newLine()
        }
        out.close()

    }

    override fun loadAndPrintStats(simName: String) {}
}