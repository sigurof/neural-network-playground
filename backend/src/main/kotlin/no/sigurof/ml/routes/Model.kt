package no.sigurof.ml.routes

import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.Record

@Serializable
class WeightsRequest(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)

@Serializable
data class TrainedNeuralNetworkResponse(
    val layers: List<MatrixDto>,
    val record: List<Record>,
)

@Serializable
data class MatrixDto(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)

@Serializable
data class NeuralNetworkParams(
    val trainingData: List<InputVsOutput>,
    val hiddenLayerDimensions: List<Int>,
)

data class WeightsDto(
    val weights: List<List<Double>>,
)
