package no.sigurof.ml.datasets

import java.io.DataInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

object MNIST {
    private const val TRAINING_LABELS_FILE = "./datasets/MNIST/unzipped/train-labels-idx1-ubyte"
    private const val TRAINING_IMAGES_FILE = "./datasets/MNIST/unzipped/train-images-idx3-ubyte"

    class TrainingLabels(
        val magicNumber: Int,
        val numberOfItems: Int,
        val labels: ByteArray,
    )

    data class TrainingImages(
        val images: List<IntArray>,
        val rows: Int,
        val cols: Int,
    )

    class Data(
        val trainingLabels: ByteArray,
        val trainingImages: List<List<Byte>>,
    )

    private fun trainingLabels(): TrainingLabels {
        return File(TRAINING_LABELS_FILE)
            .inputStream()
            .let { DataInputStream(it) }
            .use { inputStream: DataInputStream ->
                val bytes = ByteArray(8)
                val bytesRead = inputStream.read(bytes)
                require(bytesRead == 8) {
                    "File does not contain enough data."
                }
                val buffer: ByteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
                val magicNumber = buffer.int
                val numberOfItems = buffer.int
                require(magicNumber == 2049) {
                    "Magic number is expected to be 2049, but was $magicNumber."
                }
                require(numberOfItems == 60000) {
                    "Declared number of items is expected to be 60000, but was $numberOfItems."
                }
                val labels: ByteArray = inputStream.readBytes()
                require(labels.size == numberOfItems) {
                    "Declared number of items is expected to be ${labels.size}, but was $numberOfItems."
                }
                TrainingLabels(
                    magicNumber = magicNumber,
                    numberOfItems = numberOfItems,
                    labels = labels
                )
            }.also {
                println("Finished reading labels.")
            }
    }

    private fun trainingImages(n: Int): TrainingImages {
        return File(TRAINING_IMAGES_FILE)
            .inputStream()
            .let { DataInputStream(it) }
            .use { inputStream: DataInputStream ->
                val bytes = ByteArray(16)
                val bytesRead = inputStream.read(bytes)
                require(bytesRead == 16) {
                    "File does not contain enough data."
                }
                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
                val magicNumber = buffer.int
                val numberOfItems = buffer.int
                val numberOfRows = buffer.int
                val numberOfCols = buffer.int
                require(magicNumber == 2051) {
                    "Magic number is expected to be 2051, but was $magicNumber."
                }
                require(numberOfItems == 60000) {
                    "Declared number of items is expected to be 60000, but was $numberOfItems."
                }
                require(numberOfRows == 28) {
                    "Declared number of rows is expected to be 28, but was $numberOfRows."
                }
                require(numberOfCols == 28) {
                    "Declared number of cols is expected to be 28, but was $numberOfCols."
                }

                val pixelsPerImage = numberOfRows * numberOfCols
                println("start reading pixels")
                val images = mutableListOf<IntArray>()
                val numberOfImagesToInclude = min(n, numberOfItems)
                for (i in 0 until numberOfImagesToInclude) {
                    if (i % 1000 == 0) {
                        println("Read $i images.")
                    }
                    val pixels: IntArray = readUnsignedBytesToIntArray(inputStream, pixelsPerImage)
                    images.add(pixels)
                }
                println("Done reading pixels.")

//                val images: List<List<Int>> =
//                    pixels.toList()
//                        .chunked(pixelsPerImage)
                TrainingImages(
                    rows = numberOfRows,
                    cols = numberOfCols,
                    images = images
                )
            }
    }

    private fun readUnsignedBytesToIntArray(
        dataInputStream: DataInputStream,
        length: Int,
    ): IntArray {
        val result = IntArray(length)
        for (i in 0 until length) {
            // Read a signed byte and convert it to an unsigned int
            val byteRead = dataInputStream.readUnsignedByte()
            result[i] = byteRead
        }
        return result
    }

    class LabeledImage(
        val label: Byte,
        val image: IntArray,
    )

    class TrainingData(
        val imageRows: Int,
        val imageCols: Int,
        val labeledImages: List<LabeledImage>,
    )

    fun parseTrainingData(n: Int): TrainingData {
        val labels = trainingLabels().labels.take(n)
        val (images, rows, cols) = trainingImages(n)
        val map =
            labels.zip(images).map { (label, image) ->
                LabeledImage(
                    label = label,
                    image = image
                )
            }
        return TrainingData(
            imageRows = rows,
            imageCols = cols,
            labeledImages = map
        )
//        return Data(
//            trainingLabels = trainingLabels().labels,
//            trainingImages = trainingImages().images
//        )
    }

//    fun bytesToInt(
//        bytes: ByteArray,
//        offset: Int,
//    ): Int {
//        return ((bytes[offset].toInt() and 0xFF) shl 24) or
//            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
//            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
//            (bytes[offset + 3].toInt() and 0xFF)
//    }
}
