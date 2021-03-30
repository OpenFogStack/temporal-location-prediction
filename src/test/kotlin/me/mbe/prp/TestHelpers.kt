package me.mbe.prp

import me.mbe.prp.base.AlgorithmConstructor
import me.mbe.prp.base.Location
import me.mbe.prp.base.SpaceTimeLocation
import me.mbe.prp.simulation.data.*
import me.mbe.prp.simulation.helpers.GridNodeGetter
import me.mbe.prp.simulation.helpers.NodesGetter
import me.mbe.prp.simulation.helpers.ShanghaiNodesGetter
import me.mbe.prp.simulation.runSimulation
import me.mbe.prp.simulation.stats.*
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.sqrt

val positionLogFilesTestGeoLife = listOf(
    "../geolife-exploration/data/Data/000/",
    "../geolife-exploration/data/Data/001/",
    "../geolife-exploration/data/Data/002/",
)
val positionLogFilesAllGeoLife by lazy { getAllGeolifeUsers("../geolife-exploration/data/Data/") }


val positionLogFilesTestGeoLifeFST = listOf(
    "./geolife-data-2/000.out",
    "./geolife-data-2/001.out",
    "./geolife-data-2/002.out",
)

fun positionLogFilesTestGeoLifeFSTSingleUser(s: String): List<String> = listOf("./geolife-data-2/$s.out")

val positionLogFilesAllGeoLifeFST by lazy { getAllGeolifeUsers("./geolife-data-2/", fst = true) }


val positionLogFilesTestShanghai = listOf(
    "../prp-analysis/shanghai-telecom/data-out/669dca062d22edd21c6669f5a26c9cb7/",
    "../prp-analysis/shanghai-telecom/data-out/99ea9c661a8070c7322cf29390e375fc/",
    "../prp-analysis/shanghai-telecom/data-out/cbeb8a2fa95026a327520bc21df0b37c/",
)
val positionLogFilesAllShanghai by lazy { getAllShanghaiUsers("../prp-analysis/shanghai-telecom/data-out/") }


fun generateNodes(numX: Int, numY: Int): NodesGetter {
    return GridNodeGetter(Location(40.2, 116.1), Location(39.71, 116.74), numX, numY)
}

fun generateNodes(num: Int): NodesGetter {
    val n = ceil(sqrt(num.toDouble())).toInt()
    return generateNodes(n, n)
}


val defaultStatsCollector = {
    CombinedStatsCollector(
        CounterStatsCollector(),
        TimeStatsCollector(),
        NextNodeStatsCollector(),
        WrongNodeTimeStatsCollector(),
    )
}

fun getUserLocs(
    positionLogFiles: List<String>,
    fn: (String) -> Sequence<SpaceTimeLocation>
): Map<String, Iterator<SpaceTimeLocation>> {
    return positionLogFiles
        .associate { Pair(Paths.get(it).fileName.toString(), fn(it).iterator()) }
        .filter { it.value.hasNext() } //ignore empty users - should not happen
}

fun getUserLocsGeoLife(positionLogFiles: List<String>): Map<String, Iterator<SpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) {
        parseGeolifeUser(it) {
            // it.windowed(5, transform = List<SpaceTimeLocation>::average)
            it
        }
    }
}

private fun List<SpaceTimeLocation>.average(): SpaceTimeLocation {
    return SpaceTimeLocation(
        Location(
            this.map { it.location.latitudeDeg }.average(),
            this.map { it.location.longitudeDeg }.average(),
        ),
        Instant.ofEpochMilli(this.map { it.time.toEpochMilli() }.average().toLong())
    )
}

val MINUTES_5 = Duration.of(5, ChronoUnit.MINUTES)!!

fun getUserLocsGeoLifeFST(positionLogFiles: List<String>): Map<String, Iterator<SpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) {
        parseGeolifeUserFST(it) {
            //it.windowed(5, transform = List<SpaceTimeLocation>::average)
            //it
            it.zipWithNext { a, b ->
                val tDiff = Duration.between(a.time, b.time)
                if ( tDiff > MINUTES_5) {
                    val sDiff = a.location.distance(b.location) / 1000 // in km
                    if (sDiff > 1) {
                        println("jump from ${a.time}: ${a.location.latitudeDeg}, ${a.location.longitudeDeg} to ${b.time}: ${b.location.latitudeDeg}, ${b.location.longitudeDeg} ($tDiff, $sDiff km)")
                    } else {
                        println("jump from ${a.time} to ${b.time}: $tDiff")
                    }
                }
                a
            }
        }
    }
}


fun getUserLocsShanghai(positionLogFiles: List<String>): Map<String, Iterator<SpaceTimeLocation>> {
    return getUserLocs(positionLogFiles, ::parseShanghaiUser)
}

fun runSimGeoLife(
    algorithm: AlgorithmConstructor,
    simName: String,
    nodesGetter: NodesGetter,
    positionLogFiles: List<String>,
) {
    // writeCsv(nodesGetter.nodes.toPrintNodes(), "./stats-out/$simName/nodes.csv")

    runSimulation(
        algorithm,
        defaultStatsCollector(),
        nodesGetter,
        getUserLocsGeoLifeFST(positionLogFiles),
        simName,
    )
}

fun runSimGeoLife(
    algorithm: AlgorithmConstructor,
    simName: String,
    numNodes: Int = 100,
    //positionLogFiles: List<String> = positionLogFilesTestGeoLifeFST,
    positionLogFiles: List<String> = positionLogFilesTestGeoLifeFSTSingleUser("000"),
    //positionLogFiles: List<String> = positionLogFilesAllGeoLifeFST,
) {
    runSimGeoLife(algorithm, simName, generateNodes(numNodes), positionLogFiles)
}

fun runSimShanghai(
    algorithm: AlgorithmConstructor,
    simName: String,
    positionLogFiles: List<String> = positionLogFilesTestShanghai,
    //positionLogFiles: List<String> = positionLogFilesAllShanghai,
) {
    println(positionLogFiles.size)
    runSimulation(
        algorithm,
        //::simpleLatencyCalculation,
        defaultStatsCollector(),
        ShanghaiNodesGetter(loadNodesShanghai()),
        getUserLocsShanghai(positionLogFiles),
        //Duration.ZERO,
        simName,
        //isShanghai = true
    )
}
