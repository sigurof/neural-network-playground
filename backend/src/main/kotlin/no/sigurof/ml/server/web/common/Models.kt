package no.sigurof.ml.server.web.common

import kotlinx.serialization.Serializable
import no.sigurof.ml.server.web.rest.ConnectionDto

@Serializable
data class NeuralNetworkDto(
    val layerSizes: List<Int>,
    val connections: List<ConnectionDto>,
)
