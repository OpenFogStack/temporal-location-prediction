package me.mbe.prp.base

import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.Keygroup
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.time.OffsetDateTime

abstract class Algorithm(val user: User, val sim: Simulation) {
    abstract fun onStartTrip(state: WorldState)
    abstract fun onNewPosition(state: WorldState)
    abstract fun onEndTrip(state: WorldState)

    open fun onTime(state: WorldState) {}

    open fun printState() {}
    fun getKeyGroup(state: WorldState): Keygroup = state.keyGroups[user.name]!!
}

typealias AlgorithmConstructor = (user: User, sim: Simulation) -> Algorithm
