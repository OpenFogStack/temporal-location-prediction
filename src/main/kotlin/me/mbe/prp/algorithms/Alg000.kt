package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class Alg000(user: User) : Algorithm(user) {

    override fun doWork(state: WorldState): Instant {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, state.nodes.nodes)
        return state.time.plus(Duration.of(365, ChronoUnit.DAYS)) //does not need to be called again
    }
}