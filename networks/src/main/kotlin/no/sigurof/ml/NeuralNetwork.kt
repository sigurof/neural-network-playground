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
    val layers: List<Layer>,
) {
    data class Layer(
        val index: Int,
        val matrix: Matrix,
    )

    constructor(
        networkConnectionsIn: List<NetworkConnectionInfo>,
        data: DoubleArray,
    ) : this(layers = createLayers(networkConnectionsIn, data), data = data)

    companion object {
        fun createLayers(networkConnectionsIn: List<NetworkConnectionInfo>, data: DoubleArray): List<Layer> {
            val layers = mutableListOf<Layer>()
            var lastEndIndex = 0;
            for (index in networkConnectionsIn.indices) {
                val connection = networkConnectionsIn[index]
                val size = connection.weights + connection.biases
                val newEndIndex = lastEndIndex + size
                layers.add(
                    Layer(
                        index = index,
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
            return layers
        }
    }

}

class NeuralNetwork(val weightsAndBiases: WeightsAndBiases) {

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

    fun evaluateNetwork(input: DoubleArray): DoubleArray {
        val activations: MutableList<DoubleArray> = mutableListOf(input)
        for (layer in weightsAndBiases.layers) {
            val arrayProduct: DoubleArray = layer.matrix * activations[layer.index].concat(1)
            activations.add(elementwiseSigmoid(arrayProduct))
        }
        return activations.last()
    }

}

