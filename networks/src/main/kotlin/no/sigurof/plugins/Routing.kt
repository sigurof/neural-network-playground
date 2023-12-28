package no.sigurof.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.sigurof.ml.Matrix
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.models.NeuralNetworkParams
import no.sigurof.routes.customerRouting
import no.sigurof.routes.getOrderRoute
import no.sigurof.routes.listOrdersRoute
import no.sigurof.routes.totalizeOrderRoute

fun Application.configureRouting() {
    routing {
        customerRouting()
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
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
                layers = listOf(
                    listOf(firstLayer),
                    params.hiddenLayerDimensions,
                    listOf(lastLayer)
                ).flatten()
            ).train(
                trainingData = params.trainingData
            )
            val weights: List<Matrix> = neuralNetwork.weights
            call.respond(weights)
        }
    }
}
