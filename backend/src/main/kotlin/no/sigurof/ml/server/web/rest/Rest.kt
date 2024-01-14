package no.sigurof.ml.server.web.rest

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import kotlinx.serialization.Serializable
import no.sigurof.ml.neuralnetwork.NeuralNetwork
import no.sigurof.ml.neuralnetwork.NeuralNetworkBuilder
import no.sigurof.ml.neuralnetwork.NeuralNetworkConnectionSpec
import no.sigurof.ml.server.IterativeServerClientSession
import no.sigurof.ml.server.Model
import no.sigurof.ml.server.plugins.configureSerialization
import no.sigurof.ml.server.sessions
import no.sigurof.ml.server.web.common.toDto

@Serializable
class SessionDto(
    var id: String,
    var progress: Int,
    var result: String,
    var model: Model? = null,
)

fun Map.Entry<String, IterativeServerClientSession>.toDto() =
    SessionDto(
        id = this.key,
        progress = this.value.progress,
        result = this.value.result,
        model = this.value.model
    )

fun Route.restRoutes() {
    get("/ml/sessions") {
        val message: List<SessionDto> = sessions.map { it.toDto() }
        call.respond(message)
    }

    post("/ml/network") {
        val inNeuralNetwork: InNeuralNetwork = call.receive<InNeuralNetwork>()
        val shouldRecordCostFunction = call.request.queryParameters["recordCostFunction"]?.toBoolean() ?: false
        val trainingResult: NeuralNetworkBuilder.TrainingResult =
            NeuralNetworkBuilder(
                trainingData = inNeuralNetwork.trainingData,
                hiddenLayerDimensions = inNeuralNetwork.hiddenLayerDimensions
            )
                .trainNew(shouldRecordCostFunction)
        val trainedNeuralNetwork: TrainedNeuralNetworkDto = trainingResult.toDto()
        call.respond(trainedNeuralNetwork)
    }

    post("/ml/evaluate") {
        val weightsRequest = call.receive<List<InWeights>>()
        val neuralNetwork =
            NeuralNetwork(
                data = weightsRequest.flatMap { it.data.flatten() }.toDoubleArray(),
                networkConnectionsIn =
                    weightsRequest.map {
                        NeuralNetworkConnectionSpec(
                            inputs = it.columns - 1,
                            outputs = it.rows
                        )
                    }
            )
        val xPixels = 100
        val yPixels = 100
        val startX = -1.0
        val endX = 1.0
        val startY = -1.0
        val endY = 1.0
        val xStep = (endX - startX) / xPixels
        val yStep = (endY - startY) / yPixels
        val image = BufferedImage(xPixels, yPixels, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until xPixels) {
            for (y in 0 until yPixels) {
                val actualY = yPixels - y - 1
                val xValue = startX + xStep * x
                val yValue = startY + yStep * actualY
                val input = doubleArrayOf(xValue, yValue)
                val output = neuralNetwork.evaluateActivations(input).last()
                val color =
                    Color(
                        (output[0] * 255.0).toInt(),
                        0,
                        (output[1] * 255.0).toInt()
                    )
                image.setRGB(x, y, color.rgb)
            }
        }
        val outputStream = ByteArrayOutputStream()
        javax.imageio.ImageIO.write(image, "png", outputStream)
        val bytes = outputStream.toByteArray()
        call.respondBytes(bytes)
    }
}

private fun NeuralNetworkBuilder.TrainingResult.toDto(): TrainedNeuralNetworkDto {
    return TrainedNeuralNetworkDto(
        neuralNetwork = neuralNetwork.toDto(),
        record = record
    )
}

fun Application.restModule() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin) // Explicitly allow this header
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost()
    }
    configureSerialization()
}