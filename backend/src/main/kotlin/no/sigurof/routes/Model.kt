package no.sigurof.routes

import kotlinx.serialization.Serializable
import no.sigurof.ml.InputVsOutput
import no.sigurof.ml.Record

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
