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

    fun train(): NeuralNetwork {
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        val costFunctionMin = gradientDescentOld(n = weightsDimensions) { weightsVector ->
            NeuralNetwork(
                weightsAndBiases(weightsVector)
            ).calculateCostFunction(trainingData)
        }
        return NeuralNetwork(
            weightsAndBiases(costFunctionMin)
        )
    }

    private fun weightsAndBiases(data: DoubleArray): WeightsAndBiases {
        return WeightsAndBiases(
            data = data,
            networkConnectionsIn = networkConnections
        )
    }
}
