package me.mbe.prp.simulation.state

import me.mbe.prp.simulation.helpers.Capacity
import java.time.Duration

class Keygroup(val name: String, val size: Capacity, val transferTime: Duration) {
    val members: MutableMap<String, KeygroupMember> = HashMap()
}