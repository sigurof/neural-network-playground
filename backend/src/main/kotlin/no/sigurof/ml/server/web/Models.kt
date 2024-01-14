package no.sigurof.ml.server.web

import kotlinx.serialization.Serializable
import no.sigurof.ml.utils.Matrix

fun Matrix.toMatrixDto(): OutMatrix {
    val chunkedData: List<List<Double>> = this.data.toList().chunked(this.cols)
    return OutMatrix(
        rows = this.rows,
        columns = this.cols,
        data = chunkedData
    )
}

@Serializable
data class OutMatrix(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)
