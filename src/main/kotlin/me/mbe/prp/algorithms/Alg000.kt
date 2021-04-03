package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class Alg000(user: User, sim: Simulation) : Algorithm(user, sim) {
    override fun onStartTrip(state: WorldState) {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, state.nodes.nodes)
    }

    override fun onNewPosition(state: WorldState) {}

    override fun onEndTrip(state: WorldState) {}
}