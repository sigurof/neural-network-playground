package no.sigurof.ml

import kotlinx.serialization.Serializable
import no.sigurof.ml.Record


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

@Serializable
class Record(
    val step: Int,
    val cost: Double,
)

class NeuralNetworkBuilder(
    hiddenLayerDimensions: List<Int>,
    private val trainingData: List<InputVsOutput>,
) {
    val record = mutableListOf<Record>()

    private val inputLayer = trainingData.first().input.size
    private val outputLayer = trainingData.first().output.size
    private var networkConnections: List<NetworkConnectionInfo> =
        listOf(
            listOf(inputLayer),
            hiddenLayerDimensions,
            listOf(outputLayer)
        ).flatten()
            .zipWithNext { nThis, nNext -> NetworkConnectionInfo(inputs = nThis, outputs = nNext) }
    private val trainingDataChunks: List<List<InputVsOutput>> = trainingData.chunked(100)

    fun train(includeProfiling: Boolean = false): NeuralNetwork {
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        val costFunctionMin = gradientDescentOld(n = weightsDimensions,
            costFuncion = { step, weightsVector ->
                NeuralNetwork(
                    weightsAndBiases(weightsVector)
                ).calculateCostFunction(trainingDataChunks[step % trainingDataChunks.size])
            },
            iterationCallback = { step, coordinate, functionValue ->
                record.add(Record(step = step, cost = functionValue))
            }
        )
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
