package no.sigurof.ml

import kotlin.math.sqrt

fun interface Iterationcallback {
    fun invoke(step: Int, coordinate: DoubleArray, functionValue: Double)
}

fun gradientDescent(
    learningRate: Double,
    startingCoordinate: DoubleArray,
    gradientFunction: (step: Int, coordinate: DoubleArray) -> DoubleArray,
    iterationCallback: Iterationcallback? = null,
): DoubleArray {
    var coordinate = startingCoordinate.copyOf()
    var derivative = gradientFunction.invoke(0, coordinate)
    var steps = 0
    val d = 0.000003
    while (derivative.length() > d && steps < 5000) {
        println("steps = $steps, derivative = ${derivative.length()}")
        iterationCallback?.invoke(steps, coordinate, 0.0)
        val newCoordinate = DoubleArray(size = coordinate.size)

        derivative = gradientFunction.invoke(steps, coordinate)

        for (index in coordinate.indices) {
            newCoordinate[index] = coordinate[index] - learningRate * derivative[index]
        }
        coordinate = newCoordinate
        steps++;
    }
    println("steps = $steps")
    return coordinate
}

fun DoubleArray.increment(index: Int, delta: Double): DoubleArray {
    return DoubleArray(this.size) { i -> if (i == index) this[i] + delta else this[i] }
}

private fun DoubleArray.length(): Double {
    var sum = 0.0
    for (i in this.indices) {
        sum += this[i] * this[i]
    }
    return sqrt(sum)
}

typealias XY = Pair<Double, Double>

private fun Pair<Double, Double>.length(): Double = sqrt(first * first + second * second)
val Pair<Double, Double>.x: Double
    get() {
        return first
    }

val Pair<Double, Double>.y: Double
    get() {
        return second
    }
