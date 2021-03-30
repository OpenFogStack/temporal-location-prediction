package me.mbe.prp

import me.mbe.prp.simulation.helpers.MEGA_BYTE
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File


fun main() {
    transform2()
}

fun transform2() {
    File("./geolife-data-2/").mkdirs()

    val locsIterator = getUserLocsGeoLife(positionLogFilesAllGeoLife)

    locsIterator.forEach { (u, locs) ->
        println(u)
        val f = File("./geolife-data-2/$u.out")
        f.createNewFile()

        val dos = DataOutputStream(BufferedOutputStream(f.outputStream(), MEGA_BYTE.toInt()))

        var lastTime: Long? = null

        locs.forEach {
            dos.writeDouble(it.location.latitudeDeg)
            dos.writeDouble(it.location.longitudeDeg)

            val time = it.time.epochSecond

            dos.writeLong(time)

            if (lastTime != null && lastTime!! > time) {
                run {}
            }

            lastTime = time
        }

        dos.close()
    }
}

/*
fun transform1() {
    File("./geolife-data/").mkdirs()

    val locsIterator = getUserLocsGeoLife(positionLogFilesAllGeoLife)

    locsIterator.forEach { (u, locs) ->
        println(u)
        val f = File("./geolife-data/$u.out")
        f.createNewFile()

        val q = locs.asSequence().toList()

        val out = FSTObjectOutput(f.outputStream(), GeoLifeFSTConf)
        out.writeObject(q, ArrayList<SpaceTimeLocation>().javaClass)
        out.close() // required !
    }
}
*/