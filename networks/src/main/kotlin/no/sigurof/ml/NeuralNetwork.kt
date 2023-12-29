package no.sigurof.ml

import kotlin.math.exp
import kotlin.math.pow
import kotlinx.serialization.Serializable

fun elementwiseSigmoid(vector: DoubleArray): DoubleArray {
    return DoubleArray(vector.size) { index -> 1.0 / (1.0 + exp(-vector[index])) }
}

private fun DoubleArray.concat(i: Int): DoubleArray {
    return DoubleArray(this.size + i) { if (it < this.size) this[it] else 1.0 }
}

@Serializable
data class Layer(
    val index: Int,
    val matrix: Matrix,
)


class LayerSpec(
    val index: Int,
    val matrixRows: Int,
    val startIndex: Int,
    val endIndex: Int,
)

@Serializable
class WeightsAndBiases(
    val data: DoubleArray,
    val layers: List<Layer>,
) {

    constructor(
        networkConnectionsIn: List<NetworkConnectionInfo>,
        data: DoubleArray,
    ) : this(layers = something(networkConnectionsIn, data), data = data) {

    }

    companion object {
        fun something(networkConnectionsIn: List<NetworkConnectionInfo>, data: DoubleArray): List<Layer> {
            val someLayers = mutableListOf<Layer>()
            val networkConnections = mutableListOf<LayerSpec>()
            var lastEndIndex = 0;
            for (index in networkConnectionsIn.indices) {
                val connection = networkConnectionsIn[index]
                val newEndIndex = lastEndIndex + connection.weights + connection.biases
                val layerSpec = LayerSpec(
                    index = index,
                    matrixRows = connection.matrixRows,
                    startIndex = lastEndIndex,
                    endIndex = newEndIndex,
                )
                networkConnections.add(
                    layerSpec
                )
                lastEndIndex = newEndIndex
                someLayers.add(
                    Layer(
                        index = layerSpec.index,
                        matrix = Matrix(
                            rows = layerSpec.matrixRows,
                            // TODO Don't copy the array here
                            data = data.sliceArray(layerSpec.startIndex..<layerSpec.endIndex)
                        )
                    )
                )
            }
            return someLayers
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

