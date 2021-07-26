package me.mbe.prp.base

import kotlin.math.pow


fun Int.pow(i: Int): Int {
    return this.toDouble().pow(i).toInt()
}

fun Long.pow(i: Long): Long {
    return this.toDouble().pow(i.toDouble()).toLong()
}