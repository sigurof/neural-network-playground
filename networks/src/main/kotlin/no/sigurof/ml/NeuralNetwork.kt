package no.sigurof.ml

import kotlin.math.exp
import kotlin.math.pow

fun elementwiseSigmoid(vector: DoubleArray): DoubleArray {
    return DoubleArray(vector.size) { index -> 1.0 / (1.0 + exp(-vector[index])) }
}

private fun DoubleArray.concat(i: Int): DoubleArray {
    return DoubleArray(this.size + i) { if (it < this.size) this[it] else 1.0 }
}


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
        fun createLayers(networkConnectionsIn: List<NetworkConnectionInfo>, data: DoubleArray): List<WeightsLayer> {
            val weightsLayers = mutableListOf<WeightsLayer>()
            var lastEndIndex = 0;
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
                        matrix = Matrix(
                            rows = connection.matrixRows,
                            // TODO Don't copy the array here
//                            data = data.slice(lastEndIndex, newEndIndex)
                            data = data.sliceArray(lastEndIndex until newEndIndex)
                        )
                    )
                )
                lastEndIndex = newEndIndex;
            }
            return weightsLayers
        }

        fun populate(networkConnectionsIn: List<NetworkConnectionInfo>, initMethod: (Int) -> Double): WeightsAndBiases {
            return WeightsAndBiases(
                networkConnectionsIn = networkConnectionsIn,
                initMethod =    initMethod
            )
        }
    }


    constructor(networkConnectionsIn: List<NetworkConnectionInfo>, initMethod: (Int)-> Double) : this(
        networkConnectionsIn = networkConnectionsIn,
        data = DoubleArray(networkConnectionsIn.sumOf { it.weights + it.biases }, initMethod)
    )

}


class NeuralNetwork(val weightsAndBiases: WeightsAndBiases) {



    fun calculateGradient(inputVsOutputs: InputVsOutput): DoubleArray {
        val gradient = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }
        val activations: List<DoubleArray> = evaluateActivations(inputVsOutputs.input)

        // For every activation in the last activation layer
        // sum up 2(a_i^L - e_i) ∇a_i^L for every i in the last layer
        for (activationIndex in 0 until weightsAndBiases.weightsLayers.last().outputs) {
            val activation: Double = activations.last()[activationIndex]
            val expectedValue: Double = inputVsOutputs.output[activationIndex]
            val twoAMinusE: Double = 2.0 * (activation - expectedValue);
            val weightIndex: Int = weightsAndBiases.weightsLayers.lastIndex
            val gradientOfActivation: DoubleArray =
                calcGradientOfActivation(weightIndex, activationIndex, activation, activations.dropLast(1))
            for (j in gradientOfActivation.indices) {
                gradientOfActivation[j] *= twoAMinusE
                gradient[j] += gradientOfActivation[j]
            }
        }
        return gradient;
    }

    /**
     * Evaluates the formula for the gradient of each activation, which is:
     * ----------------------------------------------------------------------------
     * ∇a_i^L = a_i^L (1 - a_i^L) ( b_i^L ∑_j[ w_ij^L a_j^(L-1)]) + ∑_j[∇a_j^(L+1)]
     * ----------------------------------------------------------------------------
     * where ∑_j[ things to sum go here ] denotes a sum over the nodes of layer L-1
     * and where w and b are vectors in the many-dimensional weights and biases space.
     * */
    private fun calcGradientOfActivation(
        weightsIndex: Int,
        activationIndex: Int,
        activation: Double,
        activations: List<DoubleArray>,
    ): DoubleArray {
        // TODO Are you sure this is correct?
        val sigmoidPrime = activation * (1 - activation) // this is the derivative of the sigmoid function
        val directDependenciesGradient = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }

        val weightsLayer = weightsAndBiases.weightsLayers[weightsIndex]
        val ownMatrixStartIndex = weightsLayer.startIndex
        val cols = weightsLayer.matrix.cols
        val ownMatrixRowStartIndex = ownMatrixStartIndex + activationIndex * cols
        val biasIndex = ownMatrixRowStartIndex + cols - 1

        // 1 - Set the weights gradient contributions
        var previousLayerActivationIndex = 0
        for (col in ownMatrixRowStartIndex..<biasIndex) {
            directDependenciesGradient[col] = activations.last()[previousLayerActivationIndex] * sigmoidPrime
            previousLayerActivationIndex++;
        }

        // 2 - Set the bias's gradient contribution:
        directDependenciesGradient[biasIndex] = sigmoidPrime

        // 3 Find the gradients of each activation in the previous layer, i.e. ∑_j[∇a_j^(L+1)]
        // Also, stop recursing if we are at the first layer
        val gradientContributionsOfPreviousLayers = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }

        // TODO I'm pretty sure this cuts off correctly, since it works for the 2x2 case. However, something might be off with the arguments.
        if (weightsIndex > 0) {

            for (col in ownMatrixRowStartIndex..<biasIndex) {
                val nextActivationIndex = col - ownMatrixRowStartIndex
                // TODO Clean up this usage of the weight.
                val weight = weightsAndBiases.data[col]
                gradientContributionsOfPreviousLayers.mutablyAddElementwise(
                    calcGradientOfActivation(
                        weightsIndex = weightsIndex - 1,
                        activationIndex = nextActivationIndex,
                        activation = activations.last()[nextActivationIndex],
                        activations = activations.dropLast(1)
                    )
                )
                for (i in gradientContributionsOfPreviousLayers.indices) {
                    gradientContributionsOfPreviousLayers[i] *= (sigmoidPrime * weight)
                }

            }
//            for (nextActivationIndex in 0 until weightsAndBiases.weightsLayers[weightsIndex - 1].outputs) {
//                gradientContributionsOfPreviousLayers.mutablyAddElementwise(
//                    // TODO Move the multiplication of w_ji^L to the outside of this call
//                     calcGradientOfActivation(
//                        weightsIndex = weightsIndex - 1,
//                        activationIndex = nextActivationIndex,
//                        activation = activations.last()[nextActivationIndex],
//                        activations = activations.dropLast(1)
//                    )
//                )
//                for (i in gradientContributionsOfPreviousLayers.indices) {
//                    gradientContributionsOfPreviousLayers[i] *= sigmoidPrime;
//                }
//            }
        }

        return directDependenciesGradient.mutablyAddElementwise(gradientContributionsOfPreviousLayers)
    }

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

    fun evaluateActivations(input: DoubleArray): List<DoubleArray> {
        val activations: MutableList<DoubleArray> = mutableListOf(input)
        for (layer in weightsAndBiases.weightsLayers) {
            val arrayProduct: DoubleArray = layer.matrix * activations[layer.index].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct))
        }
        return activations
    }

    fun evaluateNetwork(input: DoubleArray): DoubleArray {
        val activations: MutableList<DoubleArray> = mutableListOf(input)
        for (layer in weightsAndBiases.weightsLayers) {
            val arrayProduct: DoubleArray = layer.matrix * activations[layer.index].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct))
        }
        return activations.last()
    }


}

fun DoubleArray.mutablyAddElementwise(gradientContributionsOfPreviousLayers: DoubleArray): DoubleArray {
    require(this.size == gradientContributionsOfPreviousLayers.size) { "The two arrays must have the same size" }
    for (index in this.indices) {
        this[index] += gradientContributionsOfPreviousLayers[index]
    }
    return this
}

