package no.sigurof.models

import kotlinx.serialization.Serializable
import no.sigurof.ml.InputVsOutput

@Serializable
data class NeuralNetworkParams(
    val trainingData: List<InputVsOutput>,
    val hiddenLayerDimensions: List<Int>,
)
