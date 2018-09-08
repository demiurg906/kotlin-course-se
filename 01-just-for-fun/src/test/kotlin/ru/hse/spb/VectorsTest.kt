package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test

class VectorsTest {
    @Test
    fun firstExample() = testAlgorithm("""
        -1 0
        0 -1
        1 0
        1 1
    """.trimIndent(), 2, 3)

    @Test
    fun secondExample() = testAlgorithm("""
        -1 0
        0 -1
        1 0
        1 1
        -4 -5
        -4 -6
    """.trimIndent(), 4, 5)

    private fun testAlgorithm(input: String, firstExpected: Int, secondExpected: Int) {
        val expected = Result(firstExpected, secondExpected)
        val lines = input.split("\n")
        val actual = parseAndFindClosestVectors(lines)
        assertEquals(expected, actual)
    }
}