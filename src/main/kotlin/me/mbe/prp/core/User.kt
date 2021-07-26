package me.mbe.prp.core

import java.time.Instant


class User(val name: String) : Positioned {
    override var location: Location = Location(0.0, 0.0)
    var lastUpdated: Instant = Instant.MIN
}
