package no.sigurof.ml

import kotlinx.serialization.Serializable


@Serializable
data class InputVsOutput(
    val input: DoubleArray,
    val output: DoubleArray,
)

class NetworkConnectionInfo(
    val inputs: Int,
    val outputs: Int,
) {

    val weights: Int
        get() = inputs * outputs
    val biases: Int
        get() = outputs
    val matrixRows: Int
        get() = outputs
    val matrixCols: Int
        get() = inputs + 1 // +1 for the bias
}

class NeuralNetworkBuilder(
    hiddenLayerDimensions: List<Int>,
    private val trainingData: List<InputVsOutput>,
) {

    private val inputLayer = trainingData.first().input.size
    private val outputLayer = trainingData.first().output.size
    private var networkConnections: List<NetworkConnectionInfo> =
        listOf(
            listOf(inputLayer),
            hiddenLayerDimensions,
            listOf(outputLayer)
        ).flatten()
            .zipWithNext { nThis, nNext -> NetworkConnectionInfo(inputs = nThis, outputs = nNext) }

    private val stuff: List<Matrix> = networkConnections.map {
        randomMatrix(
            it.matrixRows, it.matrixCols
        )
    }

    fun train(): NeuralNetwork {
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        val costFunctionMin = gradientDescentOld(n = weightsDimensions) { weightsVector ->
            NeuralNetwork(weightsVector.toMatrices(stuff))
                .calculateCostFunction(trainingData)
        }
        return NeuralNetwork(costFunctionMin.toMatrices(stuff))
    }
}

fun DoubleArray.toMatrices(randomizedWeights: List<Matrix>): List<Matrix> {
    val theWeights: MutableList<Matrix> = mutableListOf()
    var j = 0
    val weightDimensions = randomizedWeights.map { Pair(it.rows, it.cols) }
    for ((rows, cols) in weightDimensions) {
        val offset = if (j == 0) 0 else weightDimensions[j].first * weightDimensions[j].second
        val data = DoubleArray(rows * cols) { i -> this[i + offset] }
        theWeights.add(Matrix(rows, data))
        j++
    }
    return theWeights
}
