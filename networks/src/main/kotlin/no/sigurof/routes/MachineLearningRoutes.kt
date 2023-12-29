package no.sigurof.routes

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.sigurof.ml.Matrix
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.WeightsAndBiases
import no.sigurof.models.MatrixDto
import no.sigurof.models.NeuralNetworkParams


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
        val params: NeuralNetworkParams = call.receive<NeuralNetworkParams>();
        println("Received the following: ${Json.encodeToString(params)}")
        val firstLayer = params.trainingData.first().input.size
        val lastLayer = params.trainingData.first().output.size
        val neuralNetwork: NeuralNetwork = NeuralNetworkBuilder(
            trainingData = params.trainingData,
            hiddenLayerDimensions = params.hiddenLayerDimensions
        ).train()
        val weights: List<MatrixDto> = neuralNetwork.weightsAndBiases.layers.map { it.matrix.toMatrixDto() }
        call.respond(weights)
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
