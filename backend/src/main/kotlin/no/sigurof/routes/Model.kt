package no.sigurof.routes

import kotlinx.serialization.Serializable
import no.sigurof.ml.Record
import no.sigurof.models.MatrixDto

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
