package no.sigurof

import DisplayManager
import DisplayManager.Companion.HEIGHT
import DisplayManager.Companion.WIDTH
import no.sigurof.ml.InputVsOutput
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.XY
import no.sigurof.ml.visualization.Network2x2Shader
import no.sigurof.ml.visualization.Network2x3x2Shader
import no.sigurof.ml.x
import no.sigurof.ml.y
import no.sigurof.plotting.BLUE
import no.sigurof.plotting.BillboardManager
import no.sigurof.plotting.BillboardResource
import no.sigurof.plotting.Circle
import no.sigurof.plotting.RED
import no.sigurof.plotting.ShaderManager
import no.sigurof.plotting.SphereShader
import no.sigurof.plotting.engine.CoreEngine.setBackgroundColor
import org.joml.Vector2f
import org.joml.Vector4f

// Run with -XstartOnFirstThread
fun main() {
//    plot2x2Network()
    startKtorServer()
//    println("Hello, world!!!")
//    plot2x3x2Network();
}


class PosVsColor(
    val pos: XY,
    val color: String,
) {
    val colorAsVector: DoubleArray
        get() {
            return if (color == "red") doubleArrayOf(1.0, 0.0) else doubleArrayOf(0.0, 1.0)
        }
}

fun plot2x3x2Network() {
    val trainingData: List<PosVsColor> = randomlyDistributedPoints(n = 100).map { vec ->
        PosVsColor(
            pos = XY(vec.x.toDouble(), vec.y.toDouble()),
            color = if (vec.x * vec.x + vec.y * vec.y < 0.5) "blue" else "red"
        )
    }
    val circles = trainingData.map { vec ->
        val x = vec.pos.x
        val y = vec.pos.y
        val color = if (vec.color == "red") RED else BLUE
        Circle(center = Vector2f(x.toFloat(), y.toFloat()), radius = 0.01, color = color)
    }
    val realTrainingData = trainingData.map {
        InputVsOutput(
            input = doubleArrayOf(it.pos.x, it.pos.y),
            output = it.colorAsVector
        )
    }
    val network = NeuralNetworkBuilder(
        trainingData = realTrainingData,
        hiddenLayerDimensions = listOf(3),
    ).trainOld()
    val firstWeights = network.weightsAndBiases.weightsLayers[0]
    val secondWeights = network.weightsAndBiases.weightsLayers[1]
    println("Cost is ${network.calculateCostFunction(realTrainingData)}")
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader = Network2x3x2Shader()
        val circleShader = SphereShader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            setBackgroundColor(Vector4f(0.3f, 0.3f, 0.3f, 1f))
            shader.use()
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            shader.loadFirstWeights(firstWeights.matrix)
            shader.loadSecondWeights(secondWeights.matrix)
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


fun plot2x2Network() {
    val trainingData: List<PosVsColor> = randomlyDistributedPoints(n = 100).map { vec ->
        val boundaryY = 0.0
        val boundaryX = 0.0
        PosVsColor(
            pos = XY(vec.x.toDouble(), vec.y.toDouble()),
            color = if (vec.x + vec.y < boundaryX) "blue" else "red"
        )
    }
    val circles: List<Circle> = trainingData.map { vec ->
        val x = vec.pos.x
        val y = vec.pos.y
        val color = if (vec.color == "red") RED else BLUE
        Circle(center = Vector2f(x.toFloat(), y.toFloat()), radius = 0.01, color = color)
    }
    val realTrainingData = trainingData.map {
        InputVsOutput(
            input = doubleArrayOf(it.pos.x, it.pos.y),
            output = it.colorAsVector
        )
    }
    val network = NeuralNetworkBuilder(
        trainingData = realTrainingData,
        hiddenLayerDimensions = emptyList(),
    ).trainOld()
    println("Cost is ${network.calculateCostFunction(realTrainingData)}")
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader = Network2x2Shader()
        val circleShader = SphereShader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            setBackgroundColor(Vector4f(0.3f, 0.3f, 0.3f, 1f))
            shader.use()
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            shader.loadMatrix(network.weightsAndBiases.weightsLayers[0].matrix)
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




