package me.mbe.prp.algorithms

import me.mbe.prp.base.Algorithm
import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState

class Alg001(user: User, sim: Simulation) : Algorithm(user, sim) {

    override fun onStartTrip(state: WorldState) {}

    override fun onNewPosition(state: WorldState) {
        val currentNode = state.getClosestNode(user)
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf(currentNode))
    }

    override fun onEndTrip(state: WorldState) {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf())
    }
}