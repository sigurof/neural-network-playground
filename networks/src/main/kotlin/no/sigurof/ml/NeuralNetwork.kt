package no.sigurof.ml

import kotlin.math.exp
import kotlin.math.pow

fun elementwiseSigmoid(vector: DoubleArray): DoubleArray {
    return DoubleArray(vector.size) { index -> 1.0 / (1.0 + exp(-vector[index])) }
}

private fun DoubleArray.concat(i: Int): DoubleArray {
    return DoubleArray(this.size + i) { if (it < this.size) this[it] else 1.0 }
}

class NeuralNetwork(val weights: List<Matrix>) {

    internal fun calculateCostFunction(trainingData: List<InputVsOutput>): Double {
        return trainingData.map { trainingDataPoint: InputVsOutput ->
            val outputVector: DoubleArray = evaluateNetwork(trainingDataPoint.input)
            var error = 0.0
            for (i in outputVector.indices) {
                error += (outputVector[i] - trainingDataPoint.output[i]).pow(2)
            }
            error
        }.average()
    }

    fun evaluateNetwork(input: DoubleArray): DoubleArray {
        val activations: MutableList<DoubleArray> = mutableListOf(input)
        for (i in weights.indices) {
            val weightArray = weights[i]
            val arrayProduct: DoubleArray = weightArray * activations[i].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct))
        }
        return activations.last()
    }
}
