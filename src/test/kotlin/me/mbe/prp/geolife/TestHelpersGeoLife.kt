package me.mbe.prp.geolife

import me.mbe.prp.algorithms.AlgCombiner
import me.mbe.prp.base.concat
import me.mbe.prp.core.*
import me.mbe.prp.data.getAllGeolifeUsers
import me.mbe.prp.data.parseGeolifeUserFST
import me.mbe.prp.defaultMetrics
import me.mbe.prp.getUserLocs
import me.mbe.prp.nodes.GridNodeGetter
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.sqrt

val positionLogFilesTestGeoLifeFST = positionLogFilesTestGeoLifeFSTSingleUsers("000", "001", "002")

fun positionLogFilesTestGeoLifeFSTSingleUsers(vararg s: String): List<String> =
    s.map { "./geolife-data-transformed/$it.out" }

val positionLogFilesAllGeoLifeFST by lazy { getAllGeolifeUsers("./geolife-data-transformed/", fst = true) }


fun getGridNodes(numWE: Int, numNS: Int): GridNodeGetter {
    return GridNodeGetter(Location(40.2, 116.1), Location(39.71, 116.74), numWE, numNS)
}

fun getGridNodes(num: Int): GridNodeGetter {
    val n = ceil(sqrt(num.toDouble())).toInt()
    return getGridNodes(n, n)
}


fun getUserLocsGeoLifeFST(positionLogFiles: List<String>): Map<String, Sequence<AnnotatedSpaceTimeLocation>> {
    val tripSplitDuration = Duration.of(60 * 3, ChronoUnit.SECONDS)

    return getUserLocs(positionLogFiles) { userName ->
        var online = false
        var last: SpaceTimeLocation? = null //to not ignore the last; would be ignored by zipWithNext

        parseGeolifeUserFST(userName)
            .zipWithNext { current, next ->
                last = next

                var beginOfTrip = false
                var endOfTrip = false

                if (!online) {
                    online = true
                    beginOfTrip = true
                }

                if (Duration.between(current.time, next.time) > tripSplitDuration) {
                    online = false
                    endOfTrip = true
                }

                return@zipWithNext AnnotatedSpaceTimeLocation(current, beginOfTrip, endOfTrip)
            }
            .plus(sequence {
                yield(AnnotatedSpaceTimeLocation(last!!, !online, endOfTrip = true, endOfSim = true))
            })
    }

}


fun runSimGeoLife(
    algorithm: AlgorithmConstructor,
    network: Network,
    simName: String,
    extraMetrics: List<() -> MetricsCollector>,
) {

    //val positionLogFiles = positionLogFilesTestGeoLifeFST
    //val positionLogFiles = positionLogFilesTestGeoLifeFSTSingleUsers("001")
    val positionLogFiles = positionLogFilesAllGeoLifeFST

    runSimulation(
        algorithm,
        CombinedMetricsCollector(defaultMetrics.concat(extraMetrics).map { it() }),
        network,
        getUserLocsGeoLifeFST(positionLogFiles),
        simName,
        printUserStats = false,
        //printUserStats = true,
    )
}

fun runSimGeoLife(
    algorithmNNP: AlgorithmConstructor,
    algorithmSP: AlgorithmConstructor?,
    network: Network,
    simName: String,
    extraMetrics: List<() -> MetricsCollector>,
) {
    if (algorithmSP == null) {
        return runSimGeoLife(algorithmNNP, network, simName, extraMetrics)
    }

    return runSimGeoLife(
        {
            AlgCombiner(
                it,
                { it1 -> algorithmNNP(it1) },
                { it2 -> algorithmSP(it2) },
            )
        },
        network,
        simName,
        extraMetrics
    )
}


fun loadAndPrintStats(simName: String, extraMetrics: List<() -> MetricsCollector>) {
    CombinedMetricsCollector(defaultMetrics.concat(extraMetrics).map { it() }).loadAndPrintStats(simName)
}