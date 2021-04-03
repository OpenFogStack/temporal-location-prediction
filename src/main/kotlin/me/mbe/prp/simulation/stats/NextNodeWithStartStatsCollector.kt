package me.mbe.prp.simulation.stats

import me.mbe.prp.simulation.state.User
import me.mbe.prp.simulation.state.WorldState

class NextNodeWithStartStatsCollector() : NextNodeStatsCollector() {

    override val includeStarts = true

    override fun onStartTrip(user: User, state: WorldState) {
        val obj = stats.getOrPut(user.name, { StatsPerUser() })
        obj.lastNode = null
    }
}