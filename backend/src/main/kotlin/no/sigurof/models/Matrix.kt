package no.sigurof.models

import kotlinx.serialization.Serializable

@Serializable
data class MatrixDto(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)
