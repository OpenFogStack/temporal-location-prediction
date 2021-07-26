package me.mbe.prp.metrics

import me.mbe.prp.core.*


class RAMFootprintMetricsCollector : BaseMetricsCollector() {

    private val metrics: MutableMap<String, MetricsPerUser> = LinkedHashMap()

    private class MetricsPerUser(var ram: Capacity) {
        var num = 0
    }

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        val metricsOverall = MetricsPerUser(0)
        metrics.toSortedMap().forEach { (userName, state) ->
            metricsOverall.ram += state.ram
            metricsOverall.num += 1

            if (printUserStats) {
                print("User: $userName; ")
                print("RAMFootprint: ${state.ram}; ")
                println()
            }
        }
        val avgRam = metricsOverall.ram.toDouble() / metricsOverall.num.toDouble()
        val maxRam = metrics.values.maxOf { it.ram }.toDouble()

        print("User: overall; ")
        print("RAMFootprint: ${avgRam}; ")
        print("MaxRAMFootprint: ${maxRam}; ")
        println()

        writeValue(avgRam, simName)
        writeValue(maxRam, simName, "MaxRAMFootprint")
    }

    override fun loadAndPrintStats(simName: String) {
        println("RAMFootprint: ${loadValue(simName)};")
        println("MaxRAMFootprint: ${loadValue(simName, "MaxRAMFootprint")};")
    }


    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {
        metrics[user.name] = MetricsPerUser(userAlg.computeSize())
    }

}