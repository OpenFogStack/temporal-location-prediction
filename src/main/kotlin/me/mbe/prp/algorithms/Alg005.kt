package me.mbe.prp.algorithms

import me.mbe.prp.simulation.Simulation
import me.mbe.prp.simulation.state.Node
import me.mbe.prp.simulation.state.User


private val globalTransitionTable = SmartTransitionTable<Node>(2, 1)

class Alg005(
    user: User,
    sim: Simulation,
    override val transitionTable: SmartTransitionTable<Node> = globalTransitionTable
) : Alg004(user, sim, transitionTable.maxLength, transitionTable.topN) {

    override fun printState() {}
}

