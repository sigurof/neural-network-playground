package no.sigurof.ml.server.web.rest

import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.Record
import no.sigurof.ml.server.web.OutMatrix

@Serializable
internal class InWeights(
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
)

@Serializable
data class OutTrainedNeuralNetwork(
    val layers: List<OutMatrix>,
    val record: List<Record>,
)

@Serializable
internal data class InNeuralNetwork(
    val trainingData: List<InputVsOutput>,
    val hiddenLayerDimensions: List<Int>,
)
