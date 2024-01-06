package no.sigurof.routes.model

import kotlinx.serialization.Serializable

@Serializable
class WeightsInput(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)
