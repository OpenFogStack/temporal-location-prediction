package me.mbe.prp.simulation.helpers

import me.mbe.prp.base.Location
import me.mbe.prp.base.Metrics
import me.mbe.prp.base.Positioned
import java.util.Random
import java.util.BitSet
import java.util.LinkedList

class Lighthouse<R : Positioned, C : Comparable<C>>(
    private val locations: List<R>,
    private val distanceFn: (Location, Location) -> C,
    private val boundsGetter: (C) -> Pair<C, C>,
    private val useCache: Boolean = true,
    k: Int = 3
) {
    private val lighthouses = locations.indices.shuffled(Random(42)).take(k).map { i ->
        Pair(i, locations.mapIndexed { idx, node -> Pair(idx, distanceFn(node.location, locations[i].location)) }.sortedBy { it.second })
    }

    private var cacheKey: Location? = null
    private var cacheValue: Pair<R, C>? = null

    fun getClosest(x: Location): Pair<R, C> {
        if (useCache && cacheKey == x) {
            return cacheValue!!
        }

        val a = lighthouses.map { (nodeIdx, values) -> Pair(values, distanceFn(locations[nodeIdx].location, x)) }
        val b = a.map { (values, d) ->
            val (start, stop) = boundsGetter(d)
            values.findInRange(start, stop, { it.second }).map { it.first }.toBitSet()
        }

        var c: Collection<Int> = b.intersection()

        Metrics.increment("getClosest")
        Metrics.increment("getClosestSum", c.size)
        if (c.isEmpty()) {
            Metrics.increment("getClosestAll")
            c = locations.indices.toList() //search on all as backup
        }
        val result = c.map { Pair(locations[it], distanceFn(locations[it].location, x)) }.minByOrNull { it.second }!!

        if (useCache) {
            cacheKey = x
            cacheValue = result
        }

        return result
    }
}

//the list must be sorted by selector
fun <A, C : Comparable<C>> List<A>.findInRange(startInclusive: C, stopInclusive: C, selector: (A) -> C): List<A> {
    var low = 0
    var high = this.size - 1

    while (low < high) {
        val mid = (low + high) / 2
        val midVal = selector(this[mid])
        if (midVal < startInclusive) {
            low = mid + 1
        } else {
            high = mid - 1
        }
    }

    val startIndex = if (startInclusive == this[low]) low else low + 1

    for (stopIndex in startIndex until this.size) {
        if (selector(this[stopIndex]) > stopInclusive) {
            return this.subList(startIndex, stopIndex)
        }
    }

    return this.subList(startIndex, this.size)
}


fun Iterable<BitSet>.intersection(): List<Int> {
    val it = this.iterator()
    val q = it.next().clone() as BitSet
    while (it.hasNext()) {
        if (q.isEmpty) break
        q.and(it.next())
    }
    val out = LinkedList<Int>()
    q.stream().forEach { out.add(it) }
    return out
}


fun Iterable<Int>.toBitSet(): BitSet {
    val q = BitSet()
    this.forEach { q.set(it) }
    return q
}