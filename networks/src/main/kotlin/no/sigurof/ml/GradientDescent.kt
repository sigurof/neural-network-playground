package no.sigurof.ml

import kotlin.math.sqrt

fun interface Iterationcallback{

    fun invoke(step: Int, coordinate: DoubleArray, functionValue: Double)
}

fun gradientDescentOld(
    n: Int,
    costFuncion: (coord: DoubleArray) -> Double,
    iterationCallback: Iterationcallback? = null,
): DoubleArray {
    val learningRate = 6
    var startCoord = DoubleArray(n) { i -> Math.random() }
    val derivative = DoubleArray(n) { 10.0 }
    val delta = 0.0001
    var steps = 0
    val d = 0.0003
    var functionValue: Double
    while (derivative.length() > d && steps < 4000) {
        functionValue = costFuncion.invoke(startCoord)
        iterationCallback?.invoke(steps, startCoord, functionValue)
        val newCoord: DoubleArray = DoubleArray(size = startCoord.size)
        for (index in startCoord.indices) {
            val functionValueIncr = costFuncion.invoke(startCoord.increment(index, delta))
            derivative[index] = (functionValueIncr - functionValue) / delta
        }
        for (index in startCoord.indices) {
            newCoord[index] = startCoord[index] - learningRate * derivative[index]
        }
        startCoord = newCoord
        steps++;
    }
    functionValue = costFuncion.invoke(startCoord)
    iterationCallback?.invoke(steps, startCoord, functionValue)
    println("steps = $steps")
    return startCoord
}

//fun gradientDescent(n: Int, gradientFunction: (coordinate: DoubleArray) -> DoubleArray): DoubleArray {
//    val learningRate = 6
//    var startCoord = DoubleArray(n) { i -> Math.random() }
//    var derivative = DoubleArray(n) { 10.0 }
//    var steps = 0
//    val d = 0.0003
//    while (derivative.length() > d && steps < 4000) {
//        val newCoord = DoubleArray(size = startCoord.size)
//        derivative = gradientFunction.invoke(startCoord)
//        for (index in startCoord.indices) {
//            newCoord[index] = startCoord[index] - learningRate * derivative[index]
//        }
//        startCoord = newCoord
//        steps++;
//    }
//    println("steps = $steps")
//    return startCoord
//}

private fun DoubleArray.increment(index: Int, delta: Double): DoubleArray {
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
