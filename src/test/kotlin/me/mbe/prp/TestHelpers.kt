package me.mbe.prp

import me.mbe.prp.base.AlgorithmConstructor
import me.mbe.prp.base.Location
import me.mbe.prp.base.SpaceTimeLocation
import me.mbe.prp.simulation.AnnotatedSpaceTimeLocation
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
        NextNodeWithStartStatsCollector(),
        WrongNodeTimeStatsCollector(),
    )
}

fun getUserLocs(
    positionLogFiles: List<String>,
    fn: (String) -> Sequence<AnnotatedSpaceTimeLocation>
): Map<String, Iterator<AnnotatedSpaceTimeLocation>> {
    return positionLogFiles
        .associate { Pair(Paths.get(it).fileName.toString(), fn(it).iterator()) }
        .filter { it.value.hasNext() } //ignore empty users - should not happen
}

fun getUserLocsGeoLife(positionLogFiles: List<String>): Map<String, Iterator<AnnotatedSpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) {
        parseGeolifeUser(it)
            // it.windowed(5, transform = List<SpaceTimeLocation>::average)
            .map { AnnotatedSpaceTimeLocation(it, false, false) }
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

fun getUserLocsGeoLifeFST(positionLogFiles: List<String>): Map<String, Iterator<AnnotatedSpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) { userName ->
        var online = false

        var last: SpaceTimeLocation? = null //to not ignore the last; would be ignored by zipWithNext

        val q = parseGeolifeUserFST(userName)
            .zipWithNext { current, next ->
                last = next

                var beginOfTrip = false
                var endOfTrip = false

                if (!online) {
                    online = true
                    beginOfTrip = true
                }

                if (Duration.between(current.time, next.time) > MINUTES_5) {
                    online = false
                    endOfTrip = true
                }

                return@zipWithNext AnnotatedSpaceTimeLocation(current, beginOfTrip, endOfTrip)
            }
            .plus(sequence { yield(AnnotatedSpaceTimeLocation(last!!, !online, true)) })

        //to make sure everything is in order
        var onlineCheck = false
        q.onEach {
            if (it.beginOfTrip) {
                if (onlineCheck) throw RuntimeException(userName)
                onlineCheck = true
            }

            if (!onlineCheck) throw RuntimeException(userName)

            if (it.endOfTrip) {
                if (!onlineCheck) throw RuntimeException(userName)
                onlineCheck = false
            }
        }
    }

}


fun getUserLocsShanghai(positionLogFiles: List<String>): Map<String, Iterator<AnnotatedSpaceTimeLocation>> {
    return getUserLocs(positionLogFiles) {
        parseShanghaiUser(it).map {
            AnnotatedSpaceTimeLocation(
                it,
                false,
                false
            )
        } //todo: use the real time from the dataset
    }
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
    //positionLogFiles: List<String> = positionLogFilesTestGeoLifeFSTSingleUser("000"),
    positionLogFiles: List<String> = positionLogFilesAllGeoLifeFST,
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
        defaultStatsCollector(),
        ShanghaiNodesGetter(loadNodesShanghai()),
        getUserLocsShanghai(positionLogFiles),
        simName,
    )
}
