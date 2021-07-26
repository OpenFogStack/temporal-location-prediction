package me.mbe.prp.geolife

import me.mbe.prp.base.ifNullThrow
import me.mbe.prp.core.AlgorithmConstructor
import me.mbe.prp.core.MetricsCollector
import me.mbe.prp.core.Network
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import java.io.File

typealias AlgorithmPair = Pair<AlgorithmConstructor, AlgorithmConstructor?>


abstract class EvaluationBase {

    protected open val globalForceRun: Boolean = false

    protected abstract val networkSetups: Map<String, () -> Network>
    protected abstract val algorithms: Map<String, AlgorithmPair>

    protected open val extraMetrics: Map<String, List<() -> MetricsCollector>> = emptyMap()

    protected fun generateTests(
        networksToDo: String?,
        algorithmsToDo: String?,
        forceRun: Boolean = false
    ): List<DynamicContainer> {
        val networksToDoList = networksToDo?.split(",") ?: networkSetups.keys
        val algorithmsToDoList = algorithmsToDo?.split(",") ?: algorithms.keys

        return generateTests(networksToDoList, algorithmsToDoList, forceRun)
    }

    protected fun generateTests(
        networksToDo: Map<String, () -> Network>,
        algorithmsToDo: Map<String, AlgorithmPair>,
        forceRun: Boolean = false
    ): List<DynamicContainer> {
        return generateTests(
            networksToDo.entries.map { Pair(it.key, it.value) },
            algorithmsToDo.entries.map { Pair(it.key, it.value) },
            forceRun
        )
    }

    protected fun generateTests(
        networksToDo: Collection<Pair<String, () -> Network>>,
        algorithmsToDo: Collection<Pair<String, AlgorithmPair>>,
        forceRun: Boolean = false
    ): List<DynamicContainer> {
        println("networksToDo (${networksToDo.size}): ${networksToDo.map { it.first }}")
        println("algorithmsToDo (${algorithmsToDo.size}): ${algorithmsToDo.map { it.first }}")

        return networksToDo.map { ns ->
            DynamicContainer.dynamicContainer(
                ns.first,
                algorithmsToDo.map { alg ->
                    val simName = "${ns.first}/${alg.first}"
                    DynamicTest.dynamicTest(alg.first) {
                        if (forceRun || globalForceRun || !File("./stats-out/$simName/").exists())
                            runSimGeoLife(
                                alg.second.first, alg.second.second,
                                ns.second(),
                                simName, extraMetrics[simName].orEmpty()
                            )
                        else
                            loadAndPrintStats(simName, extraMetrics[simName].orEmpty())
                    }
                }
            )
        }
    }

    @JvmName("generateTests1")
    protected fun generateTests(
        networksToDo: Collection<String>,
        algorithmsToDo: Collection<String>,
        forceRun: Boolean = false
    ): List<DynamicContainer> {

        val networksToDoInt = networksToDo.map { Pair(it, networkSetups[it].ifNullThrow(it)) }
        val algorithmsToDoInt = algorithmsToDo.map { Pair(it, algorithms[it].ifNullThrow(it)) }

        return generateTests(networksToDoInt, algorithmsToDoInt, forceRun)
    }

}
