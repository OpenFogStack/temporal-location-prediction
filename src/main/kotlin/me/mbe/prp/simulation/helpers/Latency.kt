package me.mbe.prp.simulation.helpers

import java.time.Duration


typealias LatencyFunction = (distance/* meters */: Double) -> Duration


const val speedOfLight = 299_792_458.0 /* m/s */

fun simpleLatencyCalculation(distance/* meters */: Double): Duration {
    return Duration.ofNanos(((distance / (speedOfLight / 2)) * 1000 * 1000 * 1000).toLong() + 3 * 1000 * 1000)
}
