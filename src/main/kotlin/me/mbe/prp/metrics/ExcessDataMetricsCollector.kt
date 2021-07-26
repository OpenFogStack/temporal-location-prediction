package me.mbe.prp.metrics

import me.mbe.prp.core.*
import java.time.Duration
import java.time.Instant
import kotlin.collections.HashMap
import kotlin.math.max


class ExcessDataMetricsCollector : BaseMetricsCollector() {
    private val metrics: MutableMap<String, MetricsPerUser> = HashMap()

    private class MetricsPerUser {
        var lastCollect: Instant? = null
        var lastKeygroupMembers: List<KeygroupMember>? = null
        var lastClosestNode: Node? = null

        var overAllTime: Long = 0
        var wrongTime: Long = 0

        var usageTime: Long = 0
        var startTripTime: Instant = Instant.MIN
    }


    override fun onStartTrip(user: User, state: WorldState) {
        val obj = metrics.getOrPut(user.name, ExcessDataMetricsCollector::MetricsPerUser)
        obj.startTripTime = state.time
        collect(user, state, null)
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        collect(user, state, closestNode)
    }

    override fun onEndTrip(user: User, state: WorldState) {
        val obj = metrics[user.name]!!
        obj.usageTime += Duration.between(obj.startTripTime, state.time).seconds
        collect(user, state, null)
    }

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {
        collect(user, state, null)
    }

    override fun onTime(user: User, state: WorldState) {
        collect(user, state, metrics[user.name]!!.lastClosestNode /* does not change*/)
    }

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        val metricsOverall = MetricsPerUser()
        metrics.toSortedMap().forEach { (userName, state) ->
            metricsOverall.overAllTime += state.overAllTime
            metricsOverall.wrongTime += state.wrongTime
            metricsOverall.usageTime += state.usageTime
            if (printUserStats) {
                print("User: $userName; ")
                print("Time: ${state.overAllTime / (60 * 60)}h; ")
                print("UsageTime: ${state.usageTime / (60 * 60)}h; ")
                print("ExcessData: ${state.wrongTime.toDouble() / state.usageTime.toDouble()}; ")
                println()
            }
        }

        val accWrongTimeToUsage = metricsOverall.wrongTime.toDouble() / metricsOverall.usageTime.toDouble()

        print("User: overall; ")
        print("ExcessData: ${accWrongTimeToUsage}; ")
        println()

        writeValue(accWrongTimeToUsage, simName)
    }

    override fun loadAndPrintStats(simName: String) {
        println("ExcessData: ${loadValue(simName)};")
    }

    private fun collect(user: User, state: WorldState, closestNode: Node?) {
        val obj = metrics[user.name]!!

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

        val currentMembers = state.getKeygroup(user.name).members

        obj.lastCollect = state.time
        obj.lastKeygroupMembers = ArrayList(currentMembers.values) /*shallow copy*/
        obj.lastClosestNode = closestNode
    }

}