package no.sigurof.ml

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import no.sigurof.ml.datasets.MNIST
import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.NeuralNetworkBuilder

fun main() {
    val trainingData =
        MNIST.parseTrainingData(10)
            .labeledImages
            .map {
                InputVsOutput(
                    input = it.image.map { pixel -> pixel.toDouble() / 255.toDouble() }.toDoubleArray(),
                    output = it.label.asDoubleArray()
                )
            }
    val neuralNetworkBuilder =
        NeuralNetworkBuilder(
            hiddenLayerDimensions = listOf(30),
            trainingData =
            trainingData
        )
    val trainingResult =
        neuralNetworkBuilder.trainNew(recordCostFunction = true)
    println(trainingResult.record.last())
}

private fun Byte.asDoubleArray(): DoubleArray {
    val doubleArray = DoubleArray(10) { 0.0 }
    doubleArray[this.toInt()] = 1.0
    return doubleArray
}

fun writeMnistToPng() {
    val trainingData = MNIST.parseTrainingData(60000)
    trainingData.labeledImages
        .forEachIndexed { index, it ->
            val indexFormatted = String.format("%05d", index)
            if (index % 1000 == 0) {
                println("Created $indexFormatted PNGs.")
            }
            createPng(
                width = trainingData.imageCols,
                height = trainingData.imageRows,
                fileName = "./datasets/MNIST/images-and-labels/training/img-$indexFormatted-${it.label}.png",
                image = it.image
            )
        }
}

fun createPng(
    width: Int,
    height: Int,
    fileName: String,
    image: IntArray,
) {
    require(width * height == image.size) {
        "Width ($width) * height ($height) must be equal to image size (${image.size})."
    }
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value = image[y * width + x]
            val color = Color(value, value, value)
            bufferedImage.setRGB(x, y, color.rgb)
        }
    }
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", outputStream)
    File(fileName).writeBytes(outputStream.toByteArray())
}
