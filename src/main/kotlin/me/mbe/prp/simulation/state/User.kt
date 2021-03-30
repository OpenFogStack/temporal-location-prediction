package me.mbe.prp.simulation.state

import me.mbe.prp.base.Location
import me.mbe.prp.base.Positioned
import java.time.Instant
import java.time.OffsetDateTime


class User(val name: String) : Positioned {
    override var location: Location = Location(0.0, 0.0)
    var lastUpdated: Instant = Instant.MIN
}
