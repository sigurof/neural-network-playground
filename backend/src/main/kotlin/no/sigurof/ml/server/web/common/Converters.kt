package no.sigurof.ml.server.web.common

import no.sigurof.ml.neuralnetwork.NeuralNetwork
import no.sigurof.ml.neuralnetwork.PublicConnection
import no.sigurof.ml.server.web.MatrixDto
import no.sigurof.ml.server.web.rest.ConnectionDto
import no.sigurof.ml.utils.Matrix

fun NeuralNetwork.toDto(): NeuralNetworkDto {
    return NeuralNetworkDto(
        layerSizes = layerSizes,
        connections = connectionsPublic.map { it.toDto() }
    )
}

private fun PublicConnection.toDto(): ConnectionDto {
    return ConnectionDto(
        inputs = inputs,
        outputs = outputs,
        weights = weights,
        biases = biases,
        matrix = matrix.toDto()
    )
}

fun Matrix.toDto(): MatrixDto {
    val chunkedData: List<List<Double>> = this.data.toList().chunked(this.cols)
    return MatrixDto(
        rows = this.rows,
        columns = this.cols,
        data = chunkedData
    )
}
