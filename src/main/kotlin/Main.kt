package no.sigurof

import DisplayManager
import DisplayManager.Companion.HEIGHT
import DisplayManager.Companion.WIDTH
import no.sigurof.ml.NeuralNetwork
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.XY
import no.sigurof.ml.matrixOfRows
import no.sigurof.no.sigurof.ml.PosVsColor
import no.sigurof.no.sigurof.ml.leftRedRightBlue
import no.sigurof.no.sigurof.ml.visualization.Network2x2Shader
import no.sigurof.no.sigurof.plotting.BLUE
import no.sigurof.no.sigurof.plotting.BillboardManager
import no.sigurof.no.sigurof.plotting.Circle
import no.sigurof.no.sigurof.plotting.RED
import no.sigurof.no.sigurof.plotting.SphereShader
import no.sigurof.no.sigurof.plotting.engine.CoreEngine.setBackgroundColor
import no.sigurof.no.sigurof.plotting.plot
import no.sigurof.no.sigurof.randomlyDistributedPoints
import no.sigurof.plotting.BillboardResource
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import no.sigurof.plotting.ShaderManager
import org.joml.sampling.UniformSampling.Sphere

// Experiment 1
/*
* set matrix to something where you can distinguish row major and column major
* 0.0 1.0 0.0
* 0.0 0.0 0.0
* 0.0 0.0 0.0
*
* Experiment 1 showed that my assumption about the network was wrong, and I was sending in data transposed.
* This reveals that the network is actually outputting something else than a pure diagonal line.
*
* Experiment 2
* I trained the neural network again with the same data, but this time I transposed the data before sending it in.
* This showed that the classification boundary was actually slightly skewed in favor of classification.
* However, it looks like its slightly wrong. It does not look optimal. Running the simulation for longer seems to give a more sharp boundary, which lowers the cost function.
*
* Increasing the learn rate to 3.0 sped up the gradient descent dramatically but gives no improvement to the model.
* I am curious why the learning step seems to never get a cost function of less than 0.22. Additionally, the classification seems to be opposite of what it's supposed to be.
*
* If I create a better network myself, will the cost function be better as well?
*
* Experiment 3
* I was reading through my code when I realised that one of the copilot suggestions had built the circles not from the actual training data.
* Building the circles from the actual training data seemed to increase the match between the classification and the circles by a lot. Will make more attempts with this.
* In the case of setting the training data to a blue cirlce it seems the network gets optimized by just smoothing out the classification. This might be due to always being  a linear classification
*
* Suddenly, when setting the training data correctly, I am getting cost functions downwards of 0.01
*
*
*
* */



fun main() {
//    val matrix = matrixOfRows(
//        doubleArrayOf(4.0, 4.0, -5.0),
//        doubleArrayOf(-4.0, -4.0, 1.0),
//    )
    val trainingData: List<PosVsColor> = randomlyDistributedPoints(n = 100).map { vec ->
        val boundaryY = 0.5
        val boundaryX = 200
        PosVsColor(
            pos = XY(vec.x.toDouble(), vec.y.toDouble()),
            color = if (vec.x < boundaryX && vec.y < boundaryY) "blue" else "red"
        )
    }
    val circles = trainingData.map { vec ->
        val x = vec.pos.x
        val y = vec.pos.y
        val color = if (vec.color == "red") RED else BLUE
        Circle(center = Vector2f(x.toFloat(), y.toFloat()), radius = 0.01, color = color)
    }
    val network = NeuralNetworkBuilder(
        layers = listOf(
            2, 2
        )
    ).train(trainingData)
//    val network = NeuralNetwork(
//        weights = listOf(matrix)
//    )
    println("Cost is ${network.calculateCostFunction(trainingData)}")
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader = Network2x2Shader()
        val circleShader = SphereShader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            setBackgroundColor(Vector4f(0.3f, 0.3f, 0.3f, 1f))
            shader.use()
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            shader.loadMatrix(network.weights[0])
//            shader.loadMatrix(matrix)
            billboard.activate()
            billboard.render()
            circleShader.use()
            circleShader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            for (point in circles) {
                circleShader.loadCenter(point.center)
                circleShader.loadColor(point.color)
                circleShader.loadRadius(point.radius.toFloat())
                billboard.activate()
                billboard.render()
            }
            billboard.deactivate()
        }
        ShaderManager.cleanUp()
    }
}

fun plotRandomlyDistributedPoints() {
    val circles = randomlyDistributedPoints(100).map { vec ->
        val x = vec.x
        val y = vec.y
        val color = if (x > 0 && y > 0) BLUE else RED
        Circle(center = Vector2f(x, y), radius = 0.01, color = color)
    }
    plot(
        background = Vector3f(0.3f, 0.3f, 0.3f),
        data = circles
    )
}




