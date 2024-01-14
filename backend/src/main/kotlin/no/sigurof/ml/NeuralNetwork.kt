package no.sigurof.ml

import kotlin.math.exp

fun elementwiseSigmoid(vector: DoubleArray) = DoubleArray(vector.size) { index -> 1.0 / (1.0 + exp(-vector[index])) }

private fun DoubleArray.concat(i: Int) = DoubleArray(this.size + i) { if (it < this.size) this[it] else 1.0 }

class WeightsAndBiases(
    val data: DoubleArray,
    val weightsLayers: List<WeightsLayer>,
) {
    data class WeightsLayer(
        val index: Int,
        val matrix: Matrix,
        val startIndex: Int,
        val endIndex: Int,
        val inputs: Int,
        val outputs: Int,
    )

    constructor(
        networkConnectionsIn: List<NetworkConnectionInfo>,
        data: DoubleArray,
    ) : this(weightsLayers = createLayers(networkConnectionsIn, data), data = data)

    companion object {
        fun createLayers(
            networkConnectionsIn: List<NetworkConnectionInfo>,
            data: DoubleArray,
        ): List<WeightsLayer> {
            val weightsLayers = mutableListOf<WeightsLayer>()
            var lastEndIndex = 0
            for (index in networkConnectionsIn.indices) {
                val connection = networkConnectionsIn[index]
                val size = connection.weights + connection.biases
                val newEndIndex = lastEndIndex + size
                weightsLayers.add(
                    WeightsLayer(
                        index = index,
                        startIndex = lastEndIndex,
                        endIndex = newEndIndex,
                        inputs = connection.inputs,
                        outputs = connection.outputs,
                        matrix =
                            Matrix(
                                rows = connection.matrixRows,
                                // TODO Don't copy the array here
//                            data = data.slice(lastEndIndex, newEndIndex)
                                data = data.sliceArray(lastEndIndex until newEndIndex)
                            )
                    )
                )
                lastEndIndex = newEndIndex
            }
            return weightsLayers
        }

        fun populate(
            networkConnectionsIn: List<NetworkConnectionInfo>,
            initMethod: (Int) -> Double,
        ) = WeightsAndBiases(
            networkConnectionsIn = networkConnectionsIn,
            initMethod = initMethod
        )
    }

    constructor(networkConnectionsIn: List<NetworkConnectionInfo>, initMethod: (Int) -> Double) : this(
        networkConnectionsIn = networkConnectionsIn,
        data = DoubleArray(networkConnectionsIn.sumOf { it.weights + it.biases }, initMethod)
    )

    internal fun calculateCostFunction(trainingData: List<InputVsOutput>): Double {
        return trainingData.map { trainingDataPoint: InputVsOutput ->
            val outputActivations: DoubleArray = evaluateActivations(trainingDataPoint.input).last()
            var sumErrorsSquared = 0.0
            for (i in outputActivations.indices) {
                val activationMinusExpectation = outputActivations[i] - trainingDataPoint.output[i]
                sumErrorsSquared += activationMinusExpectation * activationMinusExpectation
            }
            sumErrorsSquared
        }.average()
    }

    fun evaluateActivations(input: DoubleArray): List<DoubleArray> {
        val activations: MutableList<DoubleArray> = mutableListOf(input)
        for (layer in weightsLayers) {
            val arrayProduct: DoubleArray = layer.matrix * activations[layer.index].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct))
        }
        return activations
    }
}

fun DoubleArray.mutablyAddElementwise(gradientContributionsOfPreviousLayers: DoubleArray): DoubleArray {
    require(this.size == gradientContributionsOfPreviousLayers.size) { "The two arrays must have the same size" }
    for (index in this.indices) {
        this[index] += gradientContributionsOfPreviousLayers[index]
    }
    return this
}
