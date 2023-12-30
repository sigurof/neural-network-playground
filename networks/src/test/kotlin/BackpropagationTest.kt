import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.lang.String.format
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random
import no.sigurof.ml.InputVsOutput
import no.sigurof.ml.NeuralNetworkBuilder


class BackpropagationTest : FreeSpec({


    "testing an easy case" - {

        val trainingData = listOf(
            InputVsOutput(
                input = doubleArrayOf(0.1, 0.5),
                output = doubleArrayOf(0.0, 1.0)
            )
        )
        val neuralNetworkBuilder = NeuralNetworkBuilder(
            hiddenLayerDimensions = listOf(), trainingData = trainingData
        )
        val buildNetwork = neuralNetworkBuilder
            .buildNetwork(
                doubleArrayOf(
                    0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0
                )
            )
        val contentToString = buildNetwork.evaluateNetwork(trainingData.first().input).contentToString()
        println("Network evaluated: " + contentToString + ", desired output: " + trainingData.first().output.contentToString())
        val gradient = neuralNetworkBuilder.calculateGradientInefficiently(buildNetwork, trainingData)



        println("Auto Gradient: " + gradient.contentToString())

        // Input activations
        val a0f: Double = 0.1
        val a1f: Double = 0.5

        // Final activations
        val a0L: Double = sigmoid(0.0);
        val a1L: Double = sigmoid(0.0);

        val e0: Double = 0.0;
        val e1: Double = 1.0;


        val X0: Double = 0.0;
        val X1: Double = 0.0;
        val sigmoid0: Double = sigmoid(X0);
        val sigmoid1: Double = sigmoid(X1);
        val sigmoidPrime0: Double = (1.0 - sigmoid0) * sigmoid0
        val sigmoidPrime1: Double = (1.0 - sigmoid1) * sigmoid1

        val grada0L: DoubleArray = sigmoidPrime0 * doubleArrayOf(a0f, a1f, 1.0, 0.0, 0.0, 0.0);
        val grada1L: DoubleArray = sigmoidPrime1 * doubleArrayOf(0.0, 0.0, 0.0, a0f, a1f, 1.0);

        val left = (a0L - e0) * grada0L
        val right = (a1L - e1) * grada1L
        val finalGradient = 2.0 * (left.addElementwise(right))
        println("Manually calculated gradient: " + finalGradient.contentToString())
        val buildNetwork1 = NeuralNetworkBuilder(
            hiddenLayerDimensions = listOf(), trainingData = trainingData
        )
            .buildNetwork(
                -finalGradient
            )
        val buildNetwork2 = buildNetwork1.evaluateNetwork(trainingData.first().input)
        println(
            buildNetwork2.contentToString()
        )

        // train it
        println(
            "Trained network: " + neuralNetworkBuilder.train(
                false,
                neuralNetworkBuilder::calculateGradientBackpropagation
            )
                .evaluateNetwork(trainingData.first().input).contentToString()
        )
    }


    "do the two methods of computing the gradient give the same result?" - {
        "easy case: for a 2 layers network" - {
            val trainingData = listOf(
                InputVsOutput(
                    input = doubleArrayOf(0.1, 0.5),
                    output = doubleArrayOf(0.0, 1.0)
                )
            )
            val neuralNetworkBuilder = NeuralNetworkBuilder(
                hiddenLayerDimensions = listOf(), trainingData = trainingData
            )
            val buildNetwork = neuralNetworkBuilder
                .buildNetwork(
                    doubleArrayOf(
                        0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0
                    )
                )
            val inefficientGradient = neuralNetworkBuilder.calculateGradientInefficiently(buildNetwork, trainingData)
            val backpropagationGradient =
                neuralNetworkBuilder.calculateGradientBackpropagation(buildNetwork, trainingData)
            val inefficientGradientFormatted = inefficientGradient.map { format("%.15f", it) }
            val backpropagationGradientFormatted = backpropagationGradient.map { format("%.15f", it) }
            println("Inefficient     gradient: $inefficientGradientFormatted")
            println("Backpropagation gradient: $backpropagationGradientFormatted")
            inefficientGradient.zip(backpropagationGradient).forEach { (a, b) ->
                a / b shouldBe (1.0 plusOrMinus 1e-1)
            }
        }

        "more complicated case: 3 layers" - {
            val trainingData = listOf(
                InputVsOutput(
                    input = doubleArrayOf(10.0, 5.0, 5.0),
                    output = doubleArrayOf(0.0, 1.0, 0.0)
                )
            )
            val neuralNetworkBuilder = NeuralNetworkBuilder(
                hiddenLayerDimensions = listOf(3), trainingData = trainingData
            )
            val buildNetwork = neuralNetworkBuilder.populateWeightsAndBiasesRaw {
                Random.nextDouble(-1.0, 1.0)
            }

            val inefficientGradient = neuralNetworkBuilder.calculateGradientInefficiently(buildNetwork, trainingData)
            val backpropagationGradient = neuralNetworkBuilder.calculateGradientBackpropagation(buildNetwork, trainingData)
            val inefficientGradientFormatted = inefficientGradient.map { format("%.15f", it) }
            val backpropagationGradientFormatted = backpropagationGradient.map { format("%.15f", it) }
            println("Inefficient     gradient: $inefficientGradientFormatted")
            println("Backpropagation gradient: $backpropagationGradientFormatted")
            inefficientGradient.zip(backpropagationGradient).forEach { (a, b) ->
                a / b shouldBe (1.0 plusOrMinus 1e-1)
            }
        }
    }
})

private operator fun DoubleArray.unaryMinus(): DoubleArray {
    return DoubleArray(this.size) { i -> -this[i] }
}

private fun DoubleArray.addElementwise(right: DoubleArray): DoubleArray {
    val result = DoubleArray(this.size)
    for (i in this.indices) {
        result[i] = this[i] + right[i]
    }
    return result
}

fun sigmoid(a0L: Double): Double {
    return 1.0 / (1.0 + exp(-a0L))
}

private operator fun Double.times(doubleArrayOf: DoubleArray): DoubleArray {
    return doubleArrayOf.map { this * it }.toDoubleArray()
}
