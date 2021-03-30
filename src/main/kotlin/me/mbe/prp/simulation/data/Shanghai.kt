package me.mbe.prp.simulation.data

import me.mbe.prp.base.Location
import me.mbe.prp.base.SpaceTimeLocation
import me.mbe.prp.simulation.helpers.TERRA_BYTE
import me.mbe.prp.simulation.state.Node
import java.io.File


fun parseShanghaiUser(userDir: String): Sequence<SpaceTimeLocation> {
    return File(userDir).walk()
        .filterNot { it.isDirectory }
        .filter { it.extension == "csv" }
        .sortedBy { it.name }
        .flatMap { parseShanghaiFile(it) }
}


//todo: loads all in memory, needed for usize problem
fun parseShanghaiFile(file: File): Sequence<SpaceTimeLocation> {
    val reader = file.bufferedReader()

    val list = reader.lineSequence().drop(1).map { it.split(",") }.map { line ->
        val t = line[1].split(" ")
        val l = line[2].split("/")
        SpaceTimeLocation(
            Location(l[0].toDouble(), l[1].toDouble()),
            parseDateTime(t[0], t[1]),
        )
    }.toList()

    reader.close()

    return list.asSequence()
}


fun loadNodesShanghai(): List<Node> {
    return File("../prp-analysis/shanghai-telecom/nodes.csv")
        .bufferedReader().lineSequence().drop(1).map { it.split(",") }.map { line ->
            val l = line[1].split("/")
            Node(
                line[1],
                Location(l[0].toDouble(), l[1].toDouble()), //todo: UTC is probably not correct
                TERRA_BYTE
            )
        }.toList()
}


fun getAllShanghaiUsers(baseDir: String): List<String> {
    return File(baseDir).walk().maxDepth(1).drop(1)
        .filter { it.isDirectory }.map { it.path }.sorted().toList()
}
