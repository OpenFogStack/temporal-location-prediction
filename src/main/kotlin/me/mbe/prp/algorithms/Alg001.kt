package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit


val SECOND: Duration = Duration.of(1, ChronoUnit.SECONDS)

class Alg001(user: User) : Algorithm(user) {

    override fun doWork(state: WorldState): Instant {
        val currentNode = state.getClosestNode(user)
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf(currentNode))

        return state.time.plus(SECOND)
    }
}