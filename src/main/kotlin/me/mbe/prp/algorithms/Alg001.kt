package me.mbe.prp.algorithms

import me.mbe.prp.core.*

class Alg001(p: AlgorithmParams) : Algorithm(p) {
    override fun onNewPosition(state: WorldState) {
        val currentNode = state.getClosestNode(p.user)
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf(currentNode))
    }

    override fun computeSize(): Capacity {
        return ZERO_BYTE
    }
}