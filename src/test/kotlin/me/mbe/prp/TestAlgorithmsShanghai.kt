package me.mbe.prp

import me.mbe.prp.algorithms.*

import org.junit.Test
import org.junit.Ignore
import org.junit.rules.TestName

import org.junit.Rule


class TestAlgorithmsShanghai : TestAlgorithms() {

    @Test
    fun testAlg000() = runSimShanghai(::Alg000, getTestName())

    @Test
    fun testAlg001() = runSimShanghai(::Alg001, getTestName())

    @Test
    fun testAlg002() = runSimShanghai(::Alg002, getTestName())

    @Test
    @Ignore
    fun testAlg002_all() = runSimShanghai(::Alg002, getTestName(), positionLogFilesAllShanghai)

    @Test
    fun testAlg003() = runSimShanghai(::Alg003, getTestName())

    @Test
    fun testAlg004() = runSimShanghai(::Alg004, getTestName())

    @Test
    fun testAlg005() = runSimShanghai(::Alg005, getTestName())

    @Test
    fun testAlg006() = runSimShanghai(::Alg006, getTestName())

    @Test
    fun testAlg007() = runSimShanghai(::Alg007, getTestName())
}
