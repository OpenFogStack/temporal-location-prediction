package me.mbe.prp.base

import me.mbe.prp.simulation.state.Keygroup
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Instant
import java.time.OffsetDateTime

abstract class Algorithm(val user: User) {
    abstract fun doWork(state: WorldState): Instant
    open fun printState() {}
    fun getKeyGroup(state: WorldState): Keygroup = state.keyGroups[user.name]!!
}

typealias AlgorithmConstructor = (user: User) -> Algorithm
