package no.sigurof.ml.server.web.rest

import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.Record
import no.sigurof.ml.server.web.MatrixDto
import no.sigurof.ml.server.web.common.NeuralNetworkDto

@Serializable
internal class InWeights(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)

@Serializable
data class ConnectionDto(
    val inputs: Int,
    val outputs: Int,
    val weights: Int,
    val biases: Int,
    val matrix: MatrixDto,
)

@Serializable
data class TrainedNeuralNetworkDto(
    val neuralNetwork: NeuralNetworkDto,
    val record: List<Record>,
)

@Serializable
internal data class InNeuralNetwork(
    val trainingData: List<InputVsOutput>,
    val hiddenLayerDimensions: List<Int>,
)
