package org.example

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

fun elementwiseSigmoid(vector: Array1): Array1 {
    return Array1(vector.size) { index -> 1.0 / (1.0 + exp(-vector[index])) }
}

private fun Array1.concat(i: Int): Array1 {
    return Array1(this.size + i) { if (it < this.size) this[it] else 1.0 }
}

class NeuralNetwork(private val weights: List<Matrix>) {

    constructor() : this(
        listOf<Int>(
            2,
            4,
            2
        ).zipWithNext { a, b -> randomArray2(rows = b, cols = (a + 1)) }) {
    }

    internal fun calculateCostFunction(trainingData: List<PosVsColor>): Double {
        return trainingData.map { trainingDataPoint: PosVsColor ->
            val outputVector: DoubleArray = evaluate(trainingDataPoint.value.x, trainingDataPoint.value.y).toVector()
            val expected: DoubleArray = trainingDataPoint.label.toVector()
            var error = 0.0
            for (i in outputVector.indices) {
                error += (outputVector[i] - expected[i]).pow(2)
            }
            sqrt(error)
        }.average()
    }


    fun evaluate(x: Double, y: Double): Map<String, Double> {
        val activations: MutableList<Array1> = mutableListOf(
            Array1(2) { if (it == 0) x else y }
        )
        for (i in weights.indices) {
            val weightArray = weights[i]
            val arrayProduct: Array1 = weightArray * activations[i].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct).concat(1))
        }
        return mapOf(
            "red" to activations.last()[0],
            "blue" to activations.last()[1],
        )
    }
}

private fun Map<String, Double>.toVector(): DoubleArray {
    return doubleArrayOf(
        this.getOrDefault("red", 0.0),
        this.getOrDefault("blue", 0.0),
    )
}


private fun String.toVector(): DoubleArray {
    return doubleArrayOf(
        if (this == "red") 1.0 else 0.0,
        if (this == "blue") 1.0 else 0.0,
    )
}
