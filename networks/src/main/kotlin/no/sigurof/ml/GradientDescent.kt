package no.sigurof.ml

import kotlin.math.sqrt


fun gradientDescent(n: Int, costFuncion: (coord: DoubleArray) -> Double): DoubleArray {
    val learningRate = 6
    var startCoord = DoubleArray(n) { i -> Math.random() }
    val derivative = DoubleArray(n) { 10.0 }
    val delta = 0.0001
    var steps = 0
    val d = 0.0003
    var functionValue = costFuncion.invoke(startCoord)
    while (derivative.length() > d && steps < 4000) {
        functionValue = costFuncion.invoke(startCoord)
//        println("Step $steps, derivative = ${derivative.length()}, cost = $functionValue")
        println("step = $steps, cost = $functionValue")
        val newCoord: DoubleArray = startCoord.copyOf()
        for (index in startCoord.indices) {
            val functionValueIncr = costFuncion.invoke(startCoord.increment(index, delta))
            derivative[index] = (functionValueIncr - functionValue) / delta
            newCoord[index] -= learningRate * derivative[index]
        }
        startCoord = newCoord
        steps++;
    }
    println("steps = $steps")
    return startCoord
}

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
