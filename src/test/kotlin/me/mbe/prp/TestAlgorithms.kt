package me.mbe.prp

import org.junit.rules.TestName

import org.junit.Rule


abstract class TestAlgorithms {

    @get:Rule
    val name = TestName()


    fun getTestName(): String {
        return this.javaClass.simpleName + "#" + name.methodName
    }

}
