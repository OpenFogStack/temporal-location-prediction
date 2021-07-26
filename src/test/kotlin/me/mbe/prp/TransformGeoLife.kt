package me.mbe.prp

import me.mbe.prp.base.parseDateTime
import me.mbe.prp.core.Location
import me.mbe.prp.core.MEGA_BYTE
import me.mbe.prp.core.SpaceTimeLocation
import me.mbe.prp.data.getAllGeolifeUsers
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File


fun main() {
    File("./geolife-data-transformed/").mkdirs()

    val locsIterator = getUserLocsGeoLife(getAllGeolifeUsers("./geolife-data/Data/"))

    locsIterator.forEach { (u, locs) ->
        println(u)
        val f = File("./geolife-data-transformed/$u.out")
        f.createNewFile()

        val dos = DataOutputStream(BufferedOutputStream(f.outputStream(), MEGA_BYTE.toInt()))

        var lastTime: Long? = null

        locs.forEach {
            dos.writeDouble(it.location.latitudeDeg)
            dos.writeDouble(it.location.longitudeDeg)

            val time = it.time.epochSecond

            dos.writeLong(time)

            if (lastTime != null && lastTime!! > time) {
                throw IllegalStateException("time not in sequence")
            }

            lastTime = time
        }

        dos.close()
    }
}

fun getUserLocsGeoLife(positionLogFiles: List<String>): Map<String, Sequence<SpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) { parseGeolifeUser(it) }
}

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

fun parseGeolifeUser(userDir: String): Sequence<SpaceTimeLocation> {
    return File(userDir).walk()
        .filterNot { it.isDirectory }
        .filter { it.extension == "plt" }
        .sortedBy { it.name }
        .flatMap { parseGeolifeFile(it) }
}