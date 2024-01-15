import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.lang.String.format
import kotlin.random.Random
import no.sigurof.ml.neuralnetwork.InputVsOutput
import no.sigurof.ml.neuralnetwork.NeuralNetworkBuilder
import no.sigurof.ml.neuralnetwork.backpropagation.BackPropagation

class BackpropagationTest : FreeSpec({

    "do the two methods of computing the gradient give the same result?" - {

        "easy case: for a 2 layers network" - {
            val trainingData =
                listOf(
                    InputVsOutput(
                        input = doubleArrayOf(0.1, 0.5),
                        output = doubleArrayOf(0.0, 1.0)
                    )
                )
            val neuralNetworkBuilder =
                NeuralNetworkBuilder(
                    hiddenLayerDimensions = listOf(), trainingData = trainingData
                )
            val buildNetwork =
                neuralNetworkBuilder
                    .neuralNetworkOf(
                        doubleArrayOf(
                            0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0
                        )
                    )
            val inefficientGradient =
                neuralNetworkBuilder.calculateSimpleGradient(buildNetwork, trainingData)
            val backpropagationGradient =
                BackPropagation.calculateGradient(buildNetwork, trainingData)
            val inefficientGradientFormatted = inefficientGradient.map { format("%.15f", it) }
            val backpropagationGradientFormatted = backpropagationGradient.map { format("%.15f", it) }
            println("Inefficient     gradient: $inefficientGradientFormatted")
            println("Backpropagation gradient: $backpropagationGradientFormatted")
            inefficientGradient.zip(backpropagationGradient).forEach { (a, b) ->
                a / b shouldBe (1.0 plusOrMinus 1e-1)
            }
        }

        "new backpropagation algorithm 3 layers" - {
            val trainingData =
                listOf(
                    InputVsOutput(
                        input = doubleArrayOf(10.0, 5.0, 5.0),
                        output = doubleArrayOf(0.0, 1.0, 0.0)
                    )
                )
            val neuralNetworkBuilder =
                NeuralNetworkBuilder(
                    hiddenLayerDimensions = listOf(3), trainingData = trainingData
                )
            val buildNetwork =
                neuralNetworkBuilder.populateWeightsAndBiasesRaw {
                    Random.nextDouble(-1.0, 1.0)
                }

            val inefficientGradient =
                neuralNetworkBuilder.calculateSimpleGradient(buildNetwork, trainingData)
            val backpropagationGradient =
                BackPropagation.calculateGradient(buildNetwork, trainingData)
            val inefficientGradientFormatted = inefficientGradient.map { format("%.15f", it) }
            val backpropagationGradientFormatted = backpropagationGradient.map { format("%.15f", it) }
            println("Inefficient     gradient: $inefficientGradientFormatted")
            println("Backpropagation gradient: $backpropagationGradientFormatted")
            val gradientsRatio =
                inefficientGradient.zip(backpropagationGradient).map { (a, b) ->
                    a / b
                }
            println("Ratio between gradients (should be 1.0 +- an error): $gradientsRatio")

            gradientsRatio.forEach {
                it shouldBe (1.0 plusOrMinus 1e-1)
            }
        }
    }
})

private operator fun DoubleArray.unaryMinus(): DoubleArray {
    return DoubleArray(this.size) { i -> -this[i] }
}

private operator fun Double.times(doubleArrayOf: DoubleArray): DoubleArray {
    return doubleArrayOf.map { this * it }.toDoubleArray()
}
