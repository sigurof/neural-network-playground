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
        NeuralNetwork(
            weightsAndBiases =
                WeightsAndBiases.populate(
                    networkConnectionsIn = networkConnections,
                    initMethod = initMethod
                )
        )

    fun trainOld(recordCostFunction: Boolean = false): TrainingResult =
        train(
            recordCostFunction,
            ::calculateGradientInefficiently
        )

    class TrainingResult(
        val neuralNetwork: NeuralNetwork,
        val record: List<Record>,
    )

    fun trainNew(recordCostFunction: Boolean = false): TrainingResult =
        train(recordCostFunction = recordCostFunction, gradientFunction = ::calculateGradientBackpropagationNew)

    private fun train(
        recordCostFunction: Boolean = false,
        gradientFunction: (NeuralNetwork, List<InputVsOutput>) -> DoubleArray,
    ): TrainingResult {
        val record = mutableListOf<Record>()
        val weightsDimensions = networkConnections.sumOf { it.weights + it.biases }
        var trainingDataChunk: List<InputVsOutput> = trainingDataChunks.first()
        val costFunctionMin =
            gradientDescent(
                learningRate = 10.0,
                startingCoordinate = DoubleArray(weightsDimensions) { Random.nextDouble(-1.0, 1.0) },
                gradientFunction = { step, weightsVector ->
                    trainingDataChunk = trainingDataChunks[step % trainingDataChunks.size]
                    val neuralNetwork = buildNetwork(weightsVector)
                    gradientFunction(neuralNetwork, trainingDataChunk)
                },
                iterationCallback = { step, coordinate, _ ->
                    if (recordCostFunction && step % 50 == 0) {
                        val cost = buildNetwork(coordinate).calculateCostFunction(trainingDataChunk)
                        record.add(Record(step = step, cost = cost))
                    }
                }
            )
        val neuralNetwork =
            NeuralNetwork(
                weightsAndBiases(costFunctionMin)
            )
        return TrainingResult(
            neuralNetwork = neuralNetwork,
            record = record
        )
    }

    private operator fun DoubleArray.div(size: Double): DoubleArray = DoubleArray(this.size) { i -> this[i] / size }

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
            val functionValueIncr =
                NeuralNetwork(
                    weightsAndBiases(weightsVector.increment(index, delta))
                ).calculateCostFunction(trainingDataChunk)
            derivative[index] = (functionValueIncr - functionValue) / delta
        }
        return derivative
    }

    fun buildNetwork(data: DoubleArray): NeuralNetwork = NeuralNetwork(weightsAndBiases(data))

    private fun weightsAndBiases(data: DoubleArray) =
        WeightsAndBiases(
            data = data,
            networkConnectionsIn = networkConnections
        )

    fun calculateGradientBackpropagationNew(
        network: NeuralNetwork,
        trainingData: List<InputVsOutput>,
    ): DoubleArray {
        val gradient = DoubleArray(network.weightsAndBiases.data.size) { _ -> 0.0 }
        for (inputOutput in trainingData) {
            val partGradient: DoubleArray = calculateGradientOfSample(network, inputOutput)
            gradient.mutablyAddElementwise(partGradient)
        }
        return gradient / trainingData.size.toDouble()
    }

    private fun calculateGradientOfSample(
        network: NeuralNetwork,
        inputOutput: InputVsOutput,
    ): DoubleArray {
        val activations: List<DoubleArray> = network.evaluateActivations(inputOutput.input)
        val theSumOverI = DoubleArray(network.weightsAndBiases.data.size) { _ -> 0.0 }
        for (activationIndex in 0 until network.weightsAndBiases.weightsLayers.last().outputs) {
            val activationI = activations.last()[activationIndex]
            val activationIMinusExpectedValue: Double = activationI - inputOutput.output[activationIndex]
            val gradientOfActivationI: DoubleArray =
                calculateActivationGradient(
                    network,
                    activations.dropLast(1),
                    activationIndex,
                    activationI,
                    network.weightsAndBiases.weightsLayers.lastIndex
                )
            theSumOverI.mutablyAddElementwise(activationIMinusExpectedValue * gradientOfActivationI)
        }
        return 2.0 * theSumOverI
    }

    private fun calculateActivationGradient(
        network: NeuralNetwork,
        activations: List<DoubleArray>,
        activationIndex: Int,
        activation: Double,
        weightLayerIndex: Int,
    ): DoubleArray {
        val sigmoidPrime: Double = activation * (1.0 - activation)
        val gradientOfZ: DoubleArray = calculateGradientOfZ(network, activations, activationIndex, weightLayerIndex)
        return sigmoidPrime * gradientOfZ
    }

    private fun calculateGradientOfZ(
        network: NeuralNetwork,
        activations: List<DoubleArray>,
        activationIndex: Int,
        weightLayerIndex: Int,
    ): DoubleArray {
        val gradientOfZ = DoubleArray(network.weightsAndBiases.data.size) { _ -> 0.0 }

        // Own Bias
        val numCols = network.weightsAndBiases.weightsLayers[weightLayerIndex].matrix.cols
        val currentWeightMatrixStartIndex = network.weightsAndBiases.weightsLayers[weightLayerIndex].startIndex
        val currentWeightMatrixRowStartIndex = currentWeightMatrixStartIndex + activationIndex * numCols
        val biasIndex = currentWeightMatrixRowStartIndex + numCols - 1
        gradientOfZ[biasIndex] = 1.0

        // Own Weights
        for (j in 0 until activations.last().size) {
            val weightIndex = currentWeightMatrixRowStartIndex + j
            gradientOfZ[weightIndex] = activations.last()[j]
        }

        // Gradients of activations (recursion happens here)
        if (weightLayerIndex > 0) {
            for (j in 0 until activations.last().size) {
                val activationJ = activations.last()[j]
                val weightIndex = currentWeightMatrixRowStartIndex + j
                val weight = network.weightsAndBiases.data[weightIndex]
                val gradientOfActivation: DoubleArray =
                    calculateActivationGradient(
                        network,
                        activations.dropLast(1),
                        j,
                        activationJ,
                        weightLayerIndex - 1
                    )
                gradientOfZ.mutablyAddElementwise(weight * gradientOfActivation)
            }
        }

        return gradientOfZ
    }
}

private operator fun Double.times(doubleArray: DoubleArray): DoubleArray = doubleArray.map { it * this }.toDoubleArray()
