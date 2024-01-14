package no.sigurof.ml.neuralnetwork

import kotlin.random.Random
import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.backpropagation.BackPropagation
import no.sigurof.ml.neuralnetwork.backpropagation.GradientDescent
import no.sigurof.ml.neuralnetwork.backpropagation.increment
import no.sigurof.ml.routes.NeuralNetworkParams

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

    fun populateWeightsAndBiasesRaw(initMethod: (Int) -> Double) =
        WeightsAndBiases.populate(
            networkConnectionsIn = networkConnections,
            initMethod = initMethod
        )

    class TrainingResult(
        val weightsAndBiases: WeightsAndBiases,
        val record: List<Record>,
    )

    fun trainNew(recordCostFunction: Boolean = false): TrainingResult =
        train(recordCostFunction = recordCostFunction, gradientFunction = BackPropagation::calculateGradient)

    fun trainOld(recordCostFunction: Boolean = false): TrainingResult =
        train(
            recordCostFunction,
            ::calculateSimpleGradient
        )

    fun calculateSimpleGradient(
        weightsAndBiases: WeightsAndBiases,
        trainingDataChunk: List<InputVsOutput>,
    ): DoubleArray {
        val weightsDimensions = weightsAndBiases.weightsLayers.sumOf { it.matrix.data.size }
        val functionValue = weightsAndBiases.calculateCostFunction(trainingDataChunk)
        val delta = 0.0001
        val derivative = DoubleArray(weightsDimensions) { 10.0 }
        for (index in 0 until weightsDimensions) {
            val pointIncr = weightsAndBiases.data.increment(index, delta)
            val functionValueIncr = weightsAndBiases(pointIncr).calculateCostFunction(trainingDataChunk)
            derivative[index] = (functionValueIncr - functionValue) / delta
        }
        return derivative
    }

    private fun train(
        recordCostFunction: Boolean = false,
        gradientFunction: (WeightsAndBiases, List<InputVsOutput>) -> DoubleArray,
    ): TrainingResult {
        val record = mutableListOf<Record>()
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        var trainingDataChunk: List<InputVsOutput> = trainingDataChunks.first()
        val costFunctionMin =
            GradientDescent.minimize(
                learningRate = 10.0,
                startingCoordinate = DoubleArray(weightsDimensions) { Random.nextDouble(-1.0, 1.0) },
                gradientFunction = { step, weightsVector ->
                    trainingDataChunk = trainingDataChunks[step % trainingDataChunks.size]
                    gradientFunction(weightsAndBiases(weightsVector), trainingDataChunk)
                },
                iterationCallback = { step, coordinate, _ ->
                    if (recordCostFunction && step % 50 == 0) {
                        val cost = weightsAndBiases(coordinate).calculateCostFunction(trainingDataChunk)
                        record.add(Record(step = step, cost = cost))
                    }
                }
            )
        return TrainingResult(
            weightsAndBiases = weightsAndBiases(costFunctionMin),
            record = record
        )
    }

    fun weightsAndBiases(data: DoubleArray) =
        WeightsAndBiases(
            data = data,
            networkConnectionsIn = networkConnections
        )
}
