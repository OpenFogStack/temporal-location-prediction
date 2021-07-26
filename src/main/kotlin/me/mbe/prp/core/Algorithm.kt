package me.mbe.prp.core

import java.time.Instant

abstract class Algorithm(val p: AlgorithmParams) {
    open fun onStartTrip(state: WorldState) {}
    open fun onNewPosition(state: WorldState) {}
    open fun onEndTrip(state: WorldState) {}

    open fun onTime(state: WorldState, value: String) {}

    open fun printState() {}

    abstract fun computeSize(): Capacity

    fun getKeyGroup(state: WorldState): Keygroup = state.getKeygroup(p.user.name)

    fun registerTimeCallback(at: Instant, value: String) {
        p.rTC(at, this, value)
    }

    fun cancelCallbacks(prefix: String = "") {
        p.cC(this, prefix)
    }
}

typealias RTC = (at: Instant, alg: Algorithm, value: String) -> Unit
typealias CC = (alg: Algorithm, prefix: String) -> Unit

// typealias AlgorithmConstructor = (user: User, rTC: RTC, cC: CC) -> Algorithm

typealias AlgorithmConstructor = (p: AlgorithmParams) -> Algorithm

data class AlgorithmParams(val user: User, val rTC: RTC, val cC: CC)
