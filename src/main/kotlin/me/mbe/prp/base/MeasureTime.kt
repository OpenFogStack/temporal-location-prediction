package me.mbe.prp.base

import kotlin.system.measureTimeMillis


fun <R> measureTime(name: String, block: () -> R): R {
    var out: R
    val time = measureTimeMillis {
        out = block()
    }
    println("$name took: $time ms")
    return out
}

