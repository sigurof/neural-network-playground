package no.sigurof.ml.neuralnetwork.backpropagation

import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.WeightsAndBiases
import no.sigurof.ml.neuralnetwork.mutablyAddElementwise

object BackPropagation {
    fun calculateGradient(
        weightsAndBiases: WeightsAndBiases,
        trainingData: List<InputVsOutput>,
    ): DoubleArray {
        val gradient = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }
        for (inputOutput in trainingData) {
            val partGradient: DoubleArray = calculateGradientForSample(weightsAndBiases, inputOutput)
            gradient.mutablyAddElementwise(partGradient)
        }
        return gradient / trainingData.size.toDouble()
    }

    private fun calculateGradientForSample(
        weightsAndBiases: WeightsAndBiases,
        inputOutput: InputVsOutput,
    ): DoubleArray {
        val activations: List<DoubleArray> = weightsAndBiases.evaluateActivations(inputOutput.input)
        val theSumOverI = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }
        for (activationIndex in 0 until weightsAndBiases.weightsLayers.last().outputs) {
            val activationI = activations.last()[activationIndex]
            val activationIMinusExpectedValue: Double = activationI - inputOutput.output[activationIndex]
            val gradientOfActivationI: DoubleArray =
                calculateActivationGradient(
                    weightsAndBiases,
                    activations.dropLast(1),
                    activationIndex,
                    activationI,
                    weightsAndBiases.weightsLayers.lastIndex
                )
            theSumOverI.mutablyAddElementwise(activationIMinusExpectedValue * gradientOfActivationI)
        }
        return 2.0 * theSumOverI
    }

    private fun calculateActivationGradient(
        weightsAndBiases: WeightsAndBiases,
        activations: List<DoubleArray>,
        activationIndex: Int,
        activation: Double,
        weightLayerIndex: Int,
    ): DoubleArray {
        val activationFunctionDerivative: Double = activation * (1.0 - activation)
        val gradientOfZ: DoubleArray =
            calculateGradientOfZ(weightsAndBiases, activations, activationIndex, weightLayerIndex)
        return activationFunctionDerivative * gradientOfZ
    }

    private fun calculateGradientOfZ(
        weightsAndBiases: WeightsAndBiases,
        activations: List<DoubleArray>,
        activationIndex: Int,
        weightLayerIndex: Int,
    ): DoubleArray {
        val gradientOfZ = DoubleArray(weightsAndBiases.data.size) { _ -> 0.0 }

        // Own Bias
        val numCols = weightsAndBiases.weightsLayers[weightLayerIndex].matrix.cols
        val currentWeightMatrixStartIndex = weightsAndBiases.weightsLayers[weightLayerIndex].startIndex
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
                val weight = weightsAndBiases.data[weightIndex]
                val gradientOfActivation: DoubleArray =
                    calculateActivationGradient(
                        weightsAndBiases,
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

private operator fun DoubleArray.div(size: Double): DoubleArray = DoubleArray(this.size) { i -> this[i] / size }
