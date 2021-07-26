package me.mbe.prp.algorithms.startuppred

import me.mbe.prp.core.*

class Alg013(p: AlgorithmParams) : Algorithm(p) {
    override fun onEndTrip(state: WorldState) {
        val kg = getKeyGroup(state)
        state.setKeygroupMembers(kg, listOf())
    }

    override fun computeSize(): Capacity {
        return ZERO_BYTE
    }
}
