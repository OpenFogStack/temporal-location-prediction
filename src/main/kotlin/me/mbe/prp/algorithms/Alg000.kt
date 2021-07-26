package me.mbe.prp.algorithms

import me.mbe.prp.core.*

class Alg000(p: AlgorithmParams) : Algorithm(p) {
    override fun onStartTrip(state: WorldState) {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, state.nodes.nodes)
    }

    override fun computeSize(): Capacity {
        return ZERO_BYTE
    }
}