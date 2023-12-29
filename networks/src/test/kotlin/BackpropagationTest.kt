import io.kotest.core.spec.style.FreeSpec
import no.sigurof.ml.InputVsOutput
import no.sigurof.ml.NeuralNetworkBuilder


class BackpropagationTest : FreeSpec({


    "testing an easy case" - {

        val trainingData = listOf(
            InputVsOutput(
                input = doubleArrayOf(0.0, 0.0),
                output = doubleArrayOf(0.0, 0.0),
            )
        )
//        val neuralNetworkBuilder = NeuralNetworkBuilder(
//            layers = listOf(
//                2, 2
//            )
//        )
//        neuralNetworkBuilder.train(trainingData)
    }


})
