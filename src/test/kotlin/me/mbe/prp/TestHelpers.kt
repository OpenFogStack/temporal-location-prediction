package me.mbe.prp

import me.mbe.prp.core.MetricsCollector
import me.mbe.prp.metrics.AvailabilityMetricsCollector
import me.mbe.prp.metrics.ExcessDataMetricsCollector
import me.mbe.prp.metrics.NextNodeMetricsCollector
import me.mbe.prp.metrics.StartupMetricsCollector
import me.mbe.prp.metrics.RAMFootprintMetricsCollector
import java.nio.file.Paths


val defaultMetrics = listOf<() -> MetricsCollector>(
    ::AvailabilityMetricsCollector,
    ::NextNodeMetricsCollector,
    ::StartupMetricsCollector,
    ::ExcessDataMetricsCollector,
    ::RAMFootprintMetricsCollector
)


fun <T> getUserLocs(
    positionLogFiles: List<String>,
    fn: (String) -> Sequence<T>
): Map<String, Sequence<T>> {
    return positionLogFiles
        .associate { Pair(Paths.get(it).fileName.toString(), fn(it).iterator()) }
        .filter { it.value.hasNext() } //ignore empty users - should not happen
        .mapValues { it.value.asSequence() }
}
