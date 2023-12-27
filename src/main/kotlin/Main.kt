package no.sigurof

import DisplayManager
import DisplayManager.Companion.HEIGHT
import DisplayManager.Companion.WIDTH
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.XY
import no.sigurof.ml.x
import no.sigurof.ml.y
import no.sigurof.ml.PosVsColor
import no.sigurof.ml.visualization.Network2x2Shader
import no.sigurof.ml.visualization.Network2x3x2Shader
import no.sigurof.plotting.BLUE
import no.sigurof.plotting.BillboardManager
import no.sigurof.plotting.Circle
import no.sigurof.plotting.RED
import no.sigurof.plotting.SphereShader
import no.sigurof.plotting.engine.CoreEngine.setBackgroundColor
import no.sigurof.randomlyDistributedPoints
import no.sigurof.plotting.BillboardResource
import no.sigurof.plotting.ShaderManager
import org.joml.Vector2f
import org.joml.Vector4f

fun main() {
    plot2x3x2Network();
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
    val network = NeuralNetworkBuilder(
        layers = listOf(
            2, 3, 2
        )
    ).train(trainingData)
    val firstWeights = network.weights[0]
    val secondWeights = network.weights[1]
    println("Cost is ${network.calculateCostFunction(trainingData)}")
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader = Network2x3x2Shader()
        val circleShader = SphereShader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            setBackgroundColor(Vector4f(0.3f, 0.3f, 0.3f, 1f))
            shader.use()
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            shader.loadFirstWeights(firstWeights)
            shader.loadSecondWeights(secondWeights)
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




