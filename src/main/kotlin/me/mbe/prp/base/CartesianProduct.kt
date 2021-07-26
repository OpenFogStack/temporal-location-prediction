package me.mbe.prp.base

import kotlin.reflect.KFunction

// https://stackoverflow.com/questions/53749357/idiomatic-way-to-create-n-ary-cartesian-product-combinations-of-several-sets-of
fun cartesianProduct(a: Collection<*>, b: Collection<*>, vararg sets: Collection<*>): Set<List<*>> {
    val q = listOf(a.toSet(), b.toSet()).plus(sets.map { it.toSet() })
    val z = q.fold(listOf(listOf<Any?>())) { acc, set ->
        acc.flatMap { list -> set.map { element -> list + element } }
    }
    return z.toSet()
}

fun <T> cartesianProduct(transform: KFunction<T>, a: Collection<*>, b: Collection<*>, vararg sets: Collection<*>): List<T> {
    return cartesianProduct(a, b, *sets).map {
        transform.call(*it.toTypedArray())
    }
}