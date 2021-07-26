package me.mbe.prp.core

import java.io.File

interface MetricsCollector {
    val name: String

    fun onStartTrip(user: User, state: WorldState) {}
    fun onNewPosition(user: User, state: WorldState) {}
    fun onEndTrip(user: User, state: WorldState) {}
    fun onTime(user: User, state: WorldState) {}
    fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {}

    fun printMetrics(simName: String, printUserStats: Boolean)
    fun loadAndPrintStats(simName: String)
}

class CombinedMetricsCollector(private val collectors: List<MetricsCollector>) : MetricsCollector {

    constructor(vararg collectors: MetricsCollector) : this(collectors.toList())

    override val name: String
        get() = "CombinedMetricsCollector${collectors.map(MetricsCollector::name)}"

    override fun onStartTrip(user: User, state: WorldState) {
        collectors.forEach { it.onStartTrip(user, state) }
    }

    override fun onNewPosition(user: User, state: WorldState) {
        collectors.forEach { it.onNewPosition(user, state) }
    }

    override fun onEndTrip(user: User, state: WorldState) {
        collectors.forEach { it.onEndTrip(user, state) }
    }

    override fun printMetrics(simName: String, printUserStats: Boolean) {
        collectors.forEach {
            println(it.name)
            it.printMetrics(simName, printUserStats)
        }
    }

    override fun onTime(user: User, state: WorldState) {
        collectors.iterator().forEach { it.onTime(user, state) }
    }

    override fun onEndSim(user: User, state: WorldState, userAlg: Algorithm) {
        collectors.iterator().forEach { it.onEndSim(user, state, userAlg) }
    }

    override fun loadAndPrintStats(simName: String) {
        collectors.iterator().forEach { it.loadAndPrintStats(simName) }
    }
}

abstract class BaseMetricsCollector : MetricsCollector {

    override val name: String = this.javaClass.simpleName.removeSuffix("MetricsCollector")

    open fun onNewPosition(user: User, state: WorldState, closestNode: Node) {}

    final override fun onNewPosition(user: User, state: WorldState) {
        val node = state.getClosestNode(user)
        onNewPosition(user, state, node)
    }

    protected fun writeValue(t: Double, simName: String, fileName: String = name) {
        val file = File("./stats-out/$simName/$fileName.txt")
        file.parentFile.mkdirs()

        val out = file.outputStream().bufferedWriter()
        out.write(t.toString())
        out.close()
    }

    protected fun loadValue(simName: String, fileName: String = name): Double {
        val file = File("./stats-out/$simName/$fileName.txt")

        val stream = file.inputStream().reader()
        val text = stream.readText()
        stream.close()

        return text.toDouble()
    }
}


