package me.mbe.prp.base

import java.io.Serializable
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

const val earthRadius = 6_371_000.0 // meters
const val earthDiameter = 2 * earthRadius // meters

// haversin(θ)
fun hsin(theta: Double): Double {
    return (1.0 - cos(theta)) / 2.0
}


data class Location(val latitudeDeg: Double, val longitudeDeg: Double) : Serializable {

    private val latitudeRad = Math.toRadians(latitudeDeg)
    private val longitudeRad = Math.toRadians(longitudeDeg)

    //distance in meters
    fun distance(l2: Location): Double {
        val l1 = this

        val h = hsin(l2.latitudeRad - l1.latitudeRad) +
                cos(l1.latitudeRad) * cos(l2.latitudeRad) * hsin(l2.longitudeRad - l1.longitudeRad)

        return earthDiameter * asin(sqrt(h))
    }

    fun distanceSquaredEuclid(l2: Location): Double {
        val l1 = this
        return (l1.latitudeDeg - l2.latitudeDeg).pow(2) + (l1.longitudeDeg - l2.longitudeDeg).pow(2)
    }
}


data class SpaceTimeLocation(val location: Location, val time: Instant) : Serializable


interface Positioned {
    var location: Location
}

interface Named {
    val name: String
}

interface NamedPositioned : Named, Positioned