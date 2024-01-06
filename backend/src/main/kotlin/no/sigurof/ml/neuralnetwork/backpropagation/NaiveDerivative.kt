package no.sigurof.ml.neuralnetwork.backpropagation

import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.WeightsAndBiases

object NaiveDerivative {
    fun calculateGradientInefficiently(
        weightsAndBiases: WeightsAndBiases,
        trainingDataChunk: List<InputVsOutput>,
    ): DoubleArray {
        val weightsDimensions = weightsAndBiases.weightsLayers.sumOf { it.matrix.data.size }
        val functionValue = weightsAndBiases.calculateCostFunction(trainingDataChunk)
        val delta = 0.0001
        val derivative = DoubleArray(weightsDimensions) { 10.0 }
        for (index in 0 until weightsDimensions) {
            weightsAndBiases.data.apply { this[index] += delta }
            val functionValueIncr =
                weightsAndBiases
                    .calculateCostFunction(trainingDataChunk)
            derivative[index] = (functionValueIncr - functionValue) / delta
            weightsAndBiases.data.apply { this[index] -= delta }
        }
        return derivative
    }
}
