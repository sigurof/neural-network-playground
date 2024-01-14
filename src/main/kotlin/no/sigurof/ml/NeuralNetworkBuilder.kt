package no.sigurof.ml

import no.sigurof.no.sigurof.ml.PosVsColor
import no.sigurof.ml.gradientDescent


class NeuralNetworkBuilder(
    layers: List<Int>,
) {


    private var randomizedWeights = layers.zipWithNext { a, b -> randomArray2(rows = b, cols = (a + 1)) }

    fun train(trainingData: List<PosVsColor>): NeuralNetwork {
        val weightsDimensions = randomizedWeights.sumOf { it.rows * it.cols }
        val costFunctionMin = gradientDescent(n = weightsDimensions) { weightsVector ->

            NeuralNetwork(weightsVector.toMatrices(randomizedWeights))
                .calculateCostFunction(trainingData)
        }
        return NeuralNetwork(costFunctionMin.toMatrices(randomizedWeights))
    }
}

fun DoubleArray.toMatrices(randomizedWeights: List<Matrix>): List<Matrix> {
    val theWeights: MutableList<Matrix> = mutableListOf()
    var j = 0
    val weightDimensions = randomizedWeights.map { Pair(it.rows, it.cols) }
    for ((rows, cols) in weightDimensions) {
        val offset = if (j == 0) 0 else weightDimensions[j].first * weightDimensions[j].second
        val data: DoubleArray = DoubleArray(rows * cols) { i -> this[i + offset] }
        theWeights.add(Matrix(rows, data))
        j++
    }
    return theWeights
}
