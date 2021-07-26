package me.mbe.prp.algorithms

import me.mbe.prp.core.*


class AlgCombiner(p: AlgorithmParams, vararg algorithmConstructors: AlgorithmConstructor) : Algorithm(p) {

    private val algorithms = algorithmConstructors.mapIndexed { i, algC ->
        algC(AlgorithmParams(
            p.user,
            { at, _, value ->
                registerTimeCallback(at, "$i/$value")
            },
            { _, prefix ->
                cancelCallbacks("$i/$prefix")
            }
        ))
    }

    override fun onStartTrip(state: WorldState) {
        algorithms.forEach { it.onStartTrip(state) }
    }

    override fun onNewPosition(state: WorldState) {
        algorithms.forEach { it.onNewPosition(state) }
    }

    override fun onEndTrip(state: WorldState) {
        algorithms.forEach { it.onEndTrip(state) }
    }

    override fun onTime(state: WorldState, value: String) {
        val v = value.split("/", limit = 2)
        algorithms[v[0].toInt()].onTime(state, v[1])
    }

    override fun printState() {
        algorithms.forEach { it.printState() }
    }

    override fun computeSize(): Capacity {
        return algorithms.sumOf { it.computeSize() }
    }
}