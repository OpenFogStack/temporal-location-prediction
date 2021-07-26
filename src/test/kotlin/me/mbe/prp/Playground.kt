package me.mbe.prp

import me.mbe.prp.base.cartesianProduct
import org.junit.jupiter.api.Test


class Playground {


    @Test
    fun test() {
        val a = setOf(1, 2)
        val b = setOf(1, 4)
        val c = setOf(1, 2)

        println(cartesianProduct(a, b, c))
    }
}