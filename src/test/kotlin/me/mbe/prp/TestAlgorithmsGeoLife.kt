package me.mbe.prp

import me.mbe.prp.algorithms.*
import me.mbe.prp.simulation.state.Node

import org.junit.Test
import org.junit.Ignore


class TestAlgorithmsGeoLife : TestAlgorithms() {


    @Test
    @Ignore
    fun testAlg000() = runSimGeoLife(::Alg000, getTestName())

    @Test
    fun testAlg001() = runSimGeoLife(::Alg001, getTestName())

    @Test
    fun testAlg002() = runSimGeoLife(::Alg002, getTestName())

    @Test
    @Ignore
    fun testAlg002_10000() = runSimGeoLife(::Alg002, getTestName(), numNodes = 10_000)

    @Test //should be the same as testAlg002
    @Ignore
    fun testAlg003_1() = runSimGeoLife({ u, s -> Alg003(u, s, noLastNodes = 1) }, getTestName())

    @Test //should be the same as testAlg002 with global backup
    @Ignore
    fun testAlg003_1_global_backup() {
        val globalTransitionTable = TransitionTable<List<Node>, Node>(1)
        runSimGeoLife(
            { u, s -> Alg003(u, s, noLastNodes = 1, backupTransitionTable = globalTransitionTable) },
            getTestName()
        )
    }

    @Test
    fun testAlg003() = runSimGeoLife(::Alg003, getTestName())

    @Test
    @Ignore
    fun testAlg003_400() = runSimGeoLife(::Alg003, getTestName(), numNodes = 400)

    @Test
    @Ignore
    fun testAlg003_global_backup() {
        val globalTransitionTable = TransitionTable<List<Node>, Node>(1)
        runSimGeoLife({ u, s -> Alg003(u, s, backupTransitionTable = globalTransitionTable) }, getTestName())
    }

    @Test
    fun testAlg004() = runSimGeoLife(::Alg004, getTestName())

    @Test
    @Ignore
    fun testAlg004_900() = runSimGeoLife(::Alg004, getTestName(), numNodes = 900)

    @Test
    @Ignore
    fun testAlg004_3_400() = runSimGeoLife({ u, s -> Alg004(u, s, noLastNodes = 3) }, getTestName(), numNodes = 400)

    @Test
    @Ignore
    fun testAlg004_2() = runSimGeoLife({ u, s -> Alg004(u, s, topN = 2) }, getTestName())

    @Test
    fun testAlg005() = runSimGeoLife(::Alg005, getTestName())

    @Test
    @Ignore
    fun testAlg005_3_400() {
        val globalTransitionTable = SmartTransitionTable<Node>(maxLength = 5, topN = 1)
        runSimGeoLife({ u, s -> Alg005(u, s, globalTransitionTable) }, getTestName(), numNodes = 900)
    }

    @Test
    fun testAlg006() = runSimGeoLife(::Alg006, getTestName())

    @Test
    fun testAlg007() = runSimGeoLife(::Alg007, getTestName())

    @Test
    fun testAlg008() = runSimGeoLife(::Alg008, getTestName())

    @Test
    fun testAlg009() = runSimGeoLife(::Alg009, getTestName())
}
