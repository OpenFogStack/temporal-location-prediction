package me.mbe.prp.base

import java.time.LocalTime

fun <E> Collection<E>.concat(that: Collection<E>): Collection<E> {
    val lN = ArrayList<E>(this.size + that.size)
    lN.addAll(this)
    lN.addAll(that)
    return lN
}

fun <E> Collection<E>.concat(vararg that: E): Collection<E> = this.concat(that.toList())


fun <K, V> Map<K, V>.concat(that: Map<K, V>): Map<K, V> {
    val lN = LinkedHashMap<K, V>(this.size + that.size)
    this.forEach(lN::set)
    that.forEach(lN::set)
    return lN
}


fun Iterable<LocalTime>.average(): LocalTime {
    return LocalTime.ofSecondOfDay(this.map { it.toSecondOfDay() }.average().toLong())
}

fun <E : Comparable<E>> Iterable<E>.percentile(p: Double): E {
    val s = this.sorted()
    return s[((s.size - 1) * p).toInt()]
}

fun <E : Comparable<E>> Iterable<E>.median(): E = this.percentile(0.5)
