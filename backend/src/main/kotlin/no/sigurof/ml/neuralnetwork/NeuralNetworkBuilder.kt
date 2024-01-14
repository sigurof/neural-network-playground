package no.sigurof.ml.neuralnetwork

import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.backpropagation.BackPropagation
import no.sigurof.ml.neuralnetwork.backpropagation.GradientDescent
import no.sigurof.ml.neuralnetwork.backpropagation.increment

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
    trainingData: List<InputVsOutput>,
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
    private val dimensionality = networkConnections.sumOf { it.weights + it.biases }
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
            recordCostFunction = recordCostFunction,
            ::calculateSimpleGradient
        )

    fun calculateSimpleGradient(
        weightsAndBiases: WeightsAndBiases,
        trainingDataChunk: List<InputVsOutput>,
    ): DoubleArray {
        val functionValue = weightsAndBiases.calculateCostFunction(trainingDataChunk)
        val delta = 0.0001
        val derivative = DoubleArray(dimensionality) { 10.0 }
        for (index in 0 until dimensionality) {
            val pointIncr = weightsAndBiases.data.increment(index, delta)
            val functionValueIncr = weightsAndBiases(pointIncr).calculateCostFunction(trainingDataChunk)
            derivative[index] = (functionValueIncr - functionValue) / delta
        }
        return derivative
    }

    private fun train(
        recordCostFunction: Boolean = false,
//        iterationCallback: (step: Int, coordinate: DoubleArray, functionValue: Double) -> Unit,
        gradientFunction: (WeightsAndBiases, List<InputVsOutput>) -> DoubleArray,
    ): TrainingResult {
        val record = mutableListOf<Record>()

        var trainingDataChunk: List<InputVsOutput> = trainingDataChunks.first()
        val iterationCallback: (step: Int, coordinate: DoubleArray, functionValue: Double) -> Unit =
            { step, coordinate, _ ->
                if (recordCostFunction && step % 50 == 0) {
                    val cost = weightsAndBiases(coordinate).calculateCostFunction(trainingDataChunk)
                    record.add(Record(step = step, cost = cost))
                }
            }
        val costFunctionMin =
            GradientDescent.minimize(
                learningRate = 10.0,
                startingCoordinate = DoubleArray(dimensionality) { Random.nextDouble(-1.0, 1.0) },
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

    fun trainNew2(iterationCallback: (step: Int, network: WeightsAndBiases) -> Unit): WeightsAndBiases {
        val costFunctionMin =
            GradientDescent.minimize(
                learningRate = 10.0,
                startingCoordinate = DoubleArray(dimensionality) { Random.nextDouble(-1.0, 1.0) },
                gradientFunction = { step, weightsVector ->
                    val trainingDataChunk = trainingDataChunks[step % trainingDataChunks.size]
                    BackPropagation.calculateGradient(weightsAndBiases(weightsVector), trainingDataChunk)
                },
                iterationCallback = { step, coordinate, _ ->
                    val trainingDataChunk = trainingDataChunks[step % trainingDataChunks.size]
                    val weightsAndBiases = weightsAndBiases(coordinate)
                    iterationCallback.invoke(step, weightsAndBiases)
                }
            )
        return weightsAndBiases(costFunctionMin)
    }
}

fun trainNetworkMock(startI: Int): Flow<Pair<Int, Double>> =
    flow {
        var i = startI
        while (true) {
            val sinT = sin(i.toDouble() / 5.0)
            emit(i to sinT)
            delay(300)
            i++
        }
    }
