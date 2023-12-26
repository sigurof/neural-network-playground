package no.sigurof.ml

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

fun testGradientDescent() {
    val multiMin = gradientDescent(n = 5) { coord ->
        // x1^2 + x2^2 + x3^2 + x4^2 + x5^2
        var sum = 0.0
        for (i in coord.indices) {
            sum += coord[i].pow(2)
        }
        sum

    }

    println("multiMin = $multiMin")
}

data class Vector(
    val data: DoubleArray,
) {
    fun length(): Double {
        var sum = 0.0
        for (i in data.indices) {
            sum += data[i] * data[i]
        }
        return sqrt(sum)
    }


    operator fun get(index: Int): Double {
        return data[index]
    }

    fun increment(index: Int, delta: Double): Vector {
        return Vector(data.copyOf().apply { this[index] += delta })
    }

    operator fun set(index: Int, value: Double) {
        data[index] = value
    }

}

fun gradientDescent(n: Int, function: (coord: DoubleArray) -> Double): DoubleArray {
    val alpha = 0.01
    var startCoord = DoubleArray(n) { i -> Math.random() }
    val derivative = DoubleArray(n) { 10.0 }
    val delta = 0.001
    var steps = 0
    val d = 1e-2
    while (derivative.length() > d) {
        println("Step $steps, derivative = ${derivative.length()}")
        val functionValue = function.invoke(startCoord)
        val newCoord: DoubleArray = startCoord.copyOf()
        for (index in startCoord.indices) {
            val functionValueIncr = function.invoke(startCoord.increment(index, delta))
            derivative[index] = (functionValueIncr - functionValue) / delta
            newCoord[index] -= alpha * derivative[index]
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


data class XY(
    var x: Double = 0.0,
    var y: Double = 0.0,
) {
    fun length(): Double {
        return sqrt(x * x + y * y)
    }
}

// gradient descent using array to hold coordinates


fun gradientDescent2D(function: (xy: XY) -> Double): XY {
    val alpha = 0.1;
    val xy = XY(x = 0.4, y = 0.4)
    val derivative = XY(10.0, 10.0)
    val delta = 0.001
    var steps = 0
    while (derivative.length() > 0.001) {
        val value = function.invoke(xy)
        val valueIncrX = function.invoke(XY(xy.x + delta, xy.y))
        val valueIncrY = function.invoke(XY(xy.x, xy.y + delta))
        derivative.x = (valueIncrX - value) / delta
        derivative.y = (valueIncrY - value) / delta
        xy.x -= alpha * derivative.x
        xy.y -= alpha * derivative.y
        steps++;
    }
    println("steps = $steps")
    return xy
}


fun gradientDescent1D(function: (x: Double) -> Double): Double {
    val alpha = 0.1;
    var newX = 0.4;
    var derivative = 10.0
    val delta = 0.001
    var steps = 0
    while (derivative.absoluteValue > 0.001) {
        val value = function.invoke(newX)
        val value2 = function.invoke(newX + delta)
        derivative = (value2 - value) / delta
        newX -= alpha * derivative
        steps++;
    }
    println("steps = $steps")
    return newX
}
