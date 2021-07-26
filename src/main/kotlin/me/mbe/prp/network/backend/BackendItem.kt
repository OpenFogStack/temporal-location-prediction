package me.mbe.prp.network.backend

import me.mbe.prp.core.Capacity
import java.time.Instant

class BackendItem(val name: String, val size: Capacity) {

    val pair: Pair<String, BackendItemWithAnnotation>
        get() = Pair(this.name, this.bit)

    val bit: BackendItemWithAnnotation
        get() = BackendItemWithAnnotation(this, 0, Instant.MIN)

}
