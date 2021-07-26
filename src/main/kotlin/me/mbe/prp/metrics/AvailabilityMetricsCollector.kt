package me.mbe.prp.metrics

import me.mbe.prp.base.writeCsv
import me.mbe.prp.core.*
import java.time.Duration
import java.time.Instant
import kotlin.math.max


class AvailabilityMetricsCollector : BaseMetricsCollector() {

    private val metrics: MutableMap<String, MetricsPerUser> = LinkedHashMap()

    private val metricsOverall = MetricsPerUser()

    private class MetricsPerUser {
        var lastClosestNode: Node? = null
        var lastCollect: Instant? = null
        var lastKeygroupMember: KeygroupMember? = null
        var lastChange: ChangeType = ChangeType.Start

        var overAllTime: Long = 0
        var correctTime: Long = 0

        var wrongTimeAfterStart: Long = 0
        var wrongTimeAfterMove: Long = 0

        val overTime: ArrayList<StatsOverTime> = ArrayList()
    }

    data class StatsOverTime(
        val time: Instant,
        val availability: Double
    )

    private fun computeOverallAvailability(): Double {
        val a = metrics.map { it.value.correctTime }.sum()
        val b = metrics.map { it.value.overAllTime }.sum()
        return a.toDouble() / b.toDouble()
    }

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        metrics.toSortedMap().forEach { (userName, state) ->
            metricsOverall.overAllTime += state.overAllTime
            metricsOverall.correctTime += state.correctTime
            metricsOverall.wrongTimeAfterStart += state.wrongTimeAfterStart
            metricsOverall.wrongTimeAfterMove += state.wrongTimeAfterMove

            if (printUserStats) {
                print("User: $userName; ")
                print("Time: ${state.overAllTime / (60 * 60)}h; ")
                print("Availability: ${state.correctTime.toDouble() / state.overAllTime.toDouble()}; ")
                print("Lost@Start: ${state.wrongTimeAfterStart.toDouble() / state.overAllTime.toDouble()}; ")
                print("Lost@Move: ${state.wrongTimeAfterMove.toDouble() / state.overAllTime.toDouble()}; ")
                println()
            }

            writeCsv(state.overTime, "./stats-out/$simName/AvailabilityOverTime/$userName.csv")
        }
        val accTime = metricsOverall.correctTime.toDouble() / metricsOverall.overAllTime.toDouble()
        val lostAtStart = metricsOverall.wrongTimeAfterStart.toDouble() / metricsOverall.overAllTime.toDouble()
        val lostAtMove = metricsOverall.wrongTimeAfterMove.toDouble() / metricsOverall.overAllTime.toDouble()
        print("User: overall; ")
        print("Availability: ${accTime}; ")
        print("Lost@Start: ${lostAtStart}; ")
        print("Lost@Move: ${lostAtMove}; ")
        println()

        writeValue(accTime, simName)
        writeValue(lostAtStart, simName, "Lost@Start")
        writeValue(lostAtMove, simName, "Lost@Move")

        writeCsv(metricsOverall.overTime, "./stats-out/$simName/AvailabilityOverTime/overall.csv")
    }

    override fun loadAndPrintStats(simName: String) {
        println("Availability: ${loadValue(simName)};")
        println("Lost@Start: ${loadValue(simName, "Lost@Start")};")
        println("Lost@Move: ${loadValue(simName, "Lost@Move")};")
    }

    enum class ChangeType { Start, Move }

    private fun collect(user: User, state: WorldState, closestNode: Node?) {
        val obj = metrics[user.name]!!

        if (closestNode != obj.lastClosestNode) obj.lastChange = ChangeType.Move

        if (obj.lastKeygroupMember != null) {
            val timeDiff = Duration.between(obj.lastCollect, state.time)
            obj.overAllTime += timeDiff.seconds
            val correctTime = listOf(obj.lastKeygroupMember!!)
                .filter { it.availableFrom.isBefore(state.time) }
                .map { max(obj.lastCollect!!.epochSecond, it.availableFrom.epochSecond) }
                .map { state.time.epochSecond - it }
                .sum()
            obj.correctTime += correctTime
            if (obj.lastChange == ChangeType.Move) {
                obj.wrongTimeAfterMove += timeDiff.seconds - correctTime
            } else if (obj.lastChange == ChangeType.Start) {
                obj.wrongTimeAfterStart += timeDiff.seconds - correctTime
            }
        }

        obj.lastCollect = state.time
        if (closestNode != null) {
            obj.lastKeygroupMember =
                state.getKeygroup(user.name).members[closestNode.name] ?: KeygroupMember(closestNode, Instant.MAX)
        } else {
            obj.lastKeygroupMember = null
        }
    }

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = metrics.getOrPut(user.name, { MetricsPerUser() })
        obj.lastCollect = state.time
        obj.lastClosestNode = state.getClosestNode(user)
        obj.lastChange = ChangeType.Start
    }

    override fun onNewPosition(user: User, state: WorldState, closestNode: Node) {
        collect(user, state, closestNode)
    }

    override fun onEndTrip(user: User, state: WorldState) {
        collect(user, state, null)

        val obj = metrics[user.name]!!
        obj.overTime.add(StatsOverTime(state.time, obj.correctTime.toDouble() / obj.overAllTime.toDouble()))
        metricsOverall.overTime.add(StatsOverTime(state.time, computeOverallAvailability()))
    }

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {
        collect(user, state, null)
    }

    override fun onTime(user: User, state: WorldState) {
        val obj = metrics[user.name]!!
        val q = if (obj.lastKeygroupMember == null) null else state.getClosestNode(user)
        collect(user, state, q)
    }


}