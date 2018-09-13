package ru.hse.spb

import kotlin.math.abs
import kotlin.math.sign

private enum class Orientation(private val value: Int) {
    TOP(1), BOT(-1);

    fun asInt(): Int = value
}

private class Vector(
        private val x: Long,
        private val y: Long
) : Comparable<Vector> {
    override operator fun compareTo(other: Vector): Int {
        val thisOrientation = orientation()
        val otherOrientation = other.orientation()

        if (thisOrientation != otherOrientation) {
            return thisOrientation.asInt()
        }

        val res = -(this cross other)
        return res.sign
    }

    infix fun cross(other: Vector): Long {
        return x * other.y - y * other.x
    }

    infix fun dot(other: Vector): Long {
        return x * other.x + y * other.y
    }

    private fun orientation(): Orientation = if (y < 0L || y == 0L && x < 0L) Orientation.BOT else Orientation.TOP
}

private class VectorWithIndex(val vector: Vector, val index: Int) : Comparable<VectorWithIndex> {
    override operator fun compareTo(other: VectorWithIndex): Int = vector.compareTo(other.vector)
}

private class PairOfVectors(
        private val vector1: VectorWithIndex,
        private val vector2: VectorWithIndex
) : Comparable<PairOfVectors> {
    constructor(pair: Pair<VectorWithIndex, VectorWithIndex>) : this(pair.first, pair.second)

    override operator fun compareTo(other: PairOfVectors): Int {
        val p1 = Vector(
                vector1.vector dot vector2.vector,
                abs(vector1.vector cross vector2.vector)
        )

        val p2 = Vector(
                other.vector1.vector dot other.vector2.vector,
                abs(other.vector1.vector cross other.vector2.vector)
        )

        val res = -(p1 cross p2)
        return res.sign
    }

    fun toResult(): Result = Result(vector1.index, vector2.index)
}

internal data class Result(val firstIndex: Int, val secondIndex: Int) {
    override fun toString(): String {
        return "${firstIndex + 1} ${secondIndex + 1}"
    }
}

private fun findClosestVectors(vectors: List<VectorWithIndex>): Result {
    val sortedVectors = vectors.sorted()
    val possibleAnswer = sortedVectors.zipWithNext().map(::PairOfVectors).min() ?: throw IllegalArgumentException()
    val firstLastPair = PairOfVectors(sortedVectors.first(), sortedVectors.last())

    return if (firstLastPair < possibleAnswer) {
        firstLastPair.toResult()
    } else {
        possibleAnswer.toResult()
    }
}

private fun parseVector(index: Int, input: String): VectorWithIndex {
    val splitedInput = input.split(" ")

    fun parseCoordinate(i: Int) =
            splitedInput.getOrNull(i)?.toLong() ?: throw IllegalArgumentException("Incorrect input: $input")

    val x = parseCoordinate(0)
    val y = parseCoordinate(1)

    return VectorWithIndex(Vector(x, y), index)
}


internal fun parseAndFindClosestVectors(lines: List<String>): Result {
    val vectors = lines.mapIndexed(::parseVector)
    return findClosestVectors(vectors)
}

fun main(args: Array<String>) {
    val n = readLine()?.toInt() ?: error("Incorrect input")
    val lines = (1..n).map { readLine() ?: error("Incorrect input") }
    val result = parseAndFindClosestVectors(lines)
    println(result)
}
