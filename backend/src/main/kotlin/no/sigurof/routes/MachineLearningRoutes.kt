package no.sigurof.routes

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import kotlinx.serialization.Serializable
import no.sigurof.ml.Matrix
import no.sigurof.ml.NetworkConnectionInfo
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.Record
import no.sigurof.ml.WeightsAndBiases
import no.sigurof.ml.XY
import no.sigurof.models.MatrixDto
import no.sigurof.models.NeuralNetworkParams
import no.sigurof.routes.model.WeightsInput


@Serializable
data class MlResponse(
    val layers: List<MatrixDto>,
    val record: List<Record>,
) {}

fun Route.machineLearningRouting() {
    get("/") {
        call.respondText("Hello World!")
    }

    post("/ml/network") {
        val neuralNetworkParams: NeuralNetworkParams = call.receive<NeuralNetworkParams>();
        val includeProfiling = call.request.queryParameters["includeProfiling"]?.toBoolean() ?: false
        val neuralNetworkBuilder = NeuralNetworkBuilder(neuralNetworkParams)
        val neuralNetwork: NeuralNetwork = neuralNetworkBuilder.trainNew(includeProfiling = includeProfiling)
        val weights: List<MatrixDto> = neuralNetwork.weightsAndBiases.weightsLayers.map { it.matrix.toMatrixDto() }
        call.respond(MlResponse(layers = weights, record = neuralNetworkBuilder.record))
    }


    post("/ml/evaluate") {
        val weightsInput = call.receive<List<WeightsInput>>()
        val neuralNetwork = NeuralNetwork(WeightsAndBiases(
            data = weightsInput.flatMap { it.data.flatten() }.toDoubleArray(),
            networkConnectionsIn = weightsInput.map {
                NetworkConnectionInfo(
                    inputs = it.columns - 1,
                    outputs = it.rows,
                )
            }
        ))
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

                val color = Color(
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

fun Matrix.toMatrixDto(): MatrixDto {
    val chunkedData: List<List<Double>> = this.data.toList().chunked(this.cols)
    return MatrixDto(
        rows = this.rows,
        columns = this.cols,
        data = chunkedData
    )

}
