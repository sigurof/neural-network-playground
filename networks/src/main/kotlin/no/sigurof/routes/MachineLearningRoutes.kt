package no.sigurof.routes

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import no.sigurof.ml.Matrix
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.Record
import no.sigurof.models.MatrixDto
import no.sigurof.models.NeuralNetworkParams


@Serializable
data class MlResponse(
    val layers: List<MatrixDto>,
    val record: List<Record>,
) {}

fun Route.machineLearningRouting() {
    get("/") {
        call.respondText("Hello World!")
    }

    // The first step is to get the backend to return a trained neural network based on a few parameters:
    /*
    * - the training data, defined as an array of pairs of input output arrays
    *   - this gives us the desired input/output dimensions of the network
    * - the dimensions of hidden layers
    * */

    post("/ml/network") {
        val neuralNetworkParams: NeuralNetworkParams = call.receive<NeuralNetworkParams>();
        val includeProfiling = call.request.queryParameters["includeProfiling"]?.toBoolean() ?: false
        val neuralNetworkBuilder = NeuralNetworkBuilder(
            trainingData = neuralNetworkParams.trainingData,
            hiddenLayerDimensions = neuralNetworkParams.hiddenLayerDimensions
        )
        val neuralNetwork: NeuralNetwork = neuralNetworkBuilder.train2(includeProfiling = includeProfiling)

        val weights: List<MatrixDto> = neuralNetwork.weightsAndBiases.weightsLayers.map { it.matrix.toMatrixDto() }
        call.respond(MlResponse(layers = weights, record = neuralNetworkBuilder.record))
    }
}

fun Matrix.toMatrixDto(): MatrixDto {
    val chunkedData: List<List<Double>> = this.data.toList().chunked(this.cols)
    return MatrixDto(
        rows = this.rows,
        columns = this.cols,
        data = chunkedData
    )

}
