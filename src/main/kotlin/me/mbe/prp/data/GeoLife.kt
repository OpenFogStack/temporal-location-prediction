package me.mbe.prp.data

import me.mbe.prp.core.Location
import me.mbe.prp.core.MEGA_BYTE
import me.mbe.prp.core.SpaceTimeLocation
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.time.Instant

fun parseGeolifeUserFST(fileName: String): Sequence<SpaceTimeLocation> {
    val f = File(fileName)

    val dis = DataInputStream(BufferedInputStream(f.inputStream(), MEGA_BYTE.toInt()))

    val it = object : Iterator<SpaceTimeLocation> {

        var next: SpaceTimeLocation? = null
        var nextUsed = true

        override fun hasNext(): Boolean {
            if (!nextUsed) {
                return true
            }

            try {
                val lat = dis.readDouble()
                val lon = dis.readDouble()
                val time = dis.readLong()

                val timeO = Instant.ofEpochSecond(time)

                next = SpaceTimeLocation(
                    Location(lat, lon),
                    timeO
                )
                nextUsed = false
                return true
            } catch (e: Exception) {
                return false
            }

        }

        override fun next(): SpaceTimeLocation {
            nextUsed = true
            return next!!
        }

    }

    return it.asSequence()
}


fun getAllGeolifeUsers(baseDir: String, fst: Boolean = false): List<String> {
    return File(baseDir).walk().maxDepth(1).drop(1)
        .filter { (it.isDirectory xor fst) && it.extension != "gitkeep" }.map { it.path }.sorted().toList()
}
