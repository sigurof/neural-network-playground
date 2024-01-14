package no.sigurof.ml

import kotlin.random.Random
import kotlinx.serialization.Serializable
import no.sigurof.models.NeuralNetworkParams


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
    constructor(hiddenLayerDimensions: NeuralNetworkParams) : this(
        hiddenLayerDimensions = hiddenLayerDimensions.hiddenLayerDimensions,
        trainingData = hiddenLayerDimensions.trainingData
    )

    val record = mutableListOf<Record>()

    private val inputLayer = trainingData.first().input.size
    private val outputLayer = trainingData.first().output.size
    var networkConnections: List<NetworkConnectionInfo> =
        listOf(
            listOf(inputLayer),
            hiddenLayerDimensions,
            listOf(outputLayer)
        ).flatten()
            .zipWithNext { nThis, nNext -> NetworkConnectionInfo(inputs = nThis, outputs = nNext) }
    private val trainingDataChunks: List<List<InputVsOutput>> = trainingData.chunked(100)

    fun populateWeightsAndBiasesRaw(
        initMethod: (Int) -> Double
    ): NeuralNetwork {
        return NeuralNetwork(
            weightsAndBiases = WeightsAndBiases.populate(
                networkConnectionsIn = networkConnections, initMethod = initMethod,
            )
        )

    }

    fun trainOld(
        includeProfiling: Boolean = false,
    ): NeuralNetwork {
        return train(includeProfiling, ::calculateGradientInefficiently)
    }

    fun trainNew(
        includeProfiling: Boolean = false,
    ): NeuralNetwork {
        return train(includeProfiling, ::calculateGradientBackpropagation)
    }

    fun train(
        includeProfiling: Boolean = false,
        gradientFunction: (NeuralNetwork, List<InputVsOutput>) -> DoubleArray,
    ): NeuralNetwork {
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        var trainingDataChunk: List<InputVsOutput> = trainingDataChunks.first()
        val costFunctionMin = gradientDescent(
            learningRate = 4.0,
            startingCoordinate = DoubleArray(weightsDimensions) { Random.nextDouble(-1.0, 1.0) },
            gradientFunction = { step, weightsVector ->
                trainingDataChunk = trainingDataChunks[step % trainingDataChunks.size]
                val neuralNetwork = buildNetwork(weightsVector)
                gradientFunction(neuralNetwork, trainingDataChunk)
            },
            iterationCallback = { step, coordinate, _ ->
                val cost = buildNetwork(coordinate).calculateCostFunction(trainingDataChunk)
                record.add(Record(step = step, cost = cost))
            }
        )
        return NeuralNetwork(
            weightsAndBiases(costFunctionMin)
        )
    }

    private operator fun DoubleArray.div(size: Double): DoubleArray {
        return DoubleArray(this.size) { i -> this[i] / size }
    }

    fun calculateGradientBackpropagation(
        neuralNetwork: NeuralNetwork,
        inputVsOutputs: List<InputVsOutput>,
    ): DoubleArray {
        val gradient = DoubleArray(neuralNetwork.weightsAndBiases.data.size) { _ -> 0.0 }
        for (inputVsOutput in inputVsOutputs) {
            gradient.mutablyAddElementwise(neuralNetwork.calculateGradient(inputVsOutput));
        }
        return gradient / (inputVsOutputs.size.toDouble());
    }

    fun calculateGradientInefficiently(
        neuralNetwork: NeuralNetwork,
        trainingDataChunk: List<InputVsOutput>,
    ): DoubleArray {
        val weightsDimensions = neuralNetwork.weightsAndBiases.weightsLayers.sumOf { it.matrix.data.size }
        val functionValue = neuralNetwork.calculateCostFunction(trainingDataChunk)
        val delta = 0.0001
        val derivative = DoubleArray(weightsDimensions) { 10.0 }
        val weightsVector = neuralNetwork.weightsAndBiases.data
        for (index in 0 until weightsDimensions) {
            val functionValueIncr = NeuralNetwork(
                weightsAndBiases(weightsVector.increment(index, delta))
            ).calculateCostFunction(trainingDataChunk)
            derivative[index] = (functionValueIncr - functionValue) / delta
        }
        return derivative
    }

    fun buildNetwork(data: DoubleArray): NeuralNetwork {
        return NeuralNetwork(weightsAndBiases(data))
    }

    fun weightsAndBiases(data: DoubleArray): WeightsAndBiases {
        return WeightsAndBiases(
            data = data,
            networkConnectionsIn = networkConnections
        )
    }
}

