package me.mbe.prp.simulation.data

import me.mbe.prp.base.Location
import me.mbe.prp.base.SpaceTimeLocation
import me.mbe.prp.simulation.helpers.MEGA_BYTE
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset


fun parseDateTime(date: String, time: String): Instant {
    return OffsetDateTime.of(
        date.substring(0, 4).toInt(), date.substring(5, 7).toInt(), date.substring(8, 10).toInt(),
        time.substring(0, 2).toInt(), time.substring(3, 5).toInt(), time.substring(6, 8).toInt(),
        0, ZoneOffset.UTC
    ).toInstant()

}

//todo: loads all in memory, needed for usize problem
fun parseGeolifeFile(file: File): Sequence<SpaceTimeLocation> {
    val reader = file.bufferedReader()

    val list = reader.lineSequence().drop(6).map { it.split(",") }.map { line ->
        SpaceTimeLocation(
            Location(line[0].toDouble(), line[1].toDouble()),
            parseDateTime(line[5], line[6])
        )
    }.toList()

    reader.close()

    return list.asSequence()
}

fun parseGeolifeUser(
    userDir: String,
): Sequence<SpaceTimeLocation> {
    return File(userDir).walk()
        .filterNot { it.isDirectory }
        .filter { it.extension == "plt" }
        .sortedBy { it.name }
        .flatMap { parseGeolifeFile(it) }
}

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
        .filter { it.isDirectory xor fst }.map { it.path }.sorted().toList()
}
