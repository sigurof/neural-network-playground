package no.sigurof

import DisplayManager
import DisplayManager.Companion.HEIGHT
import DisplayManager.Companion.WIDTH
import no.sigurof.ml.NeuralNetworkBuilder
import no.sigurof.ml.XY
import no.sigurof.no.sigurof.ml.PosVsColor
import no.sigurof.no.sigurof.ml.leftRedRightBlue
import no.sigurof.no.sigurof.ml.visualization.Network2x2Shader
import no.sigurof.no.sigurof.plotting.BLUE
import no.sigurof.no.sigurof.plotting.BillboardManager
import no.sigurof.no.sigurof.plotting.Circle
import no.sigurof.no.sigurof.plotting.RED
import no.sigurof.no.sigurof.plotting.engine.CoreEngine.setBackgroundColor
import no.sigurof.no.sigurof.plotting.plot
import no.sigurof.no.sigurof.randomlyDistributedPoints
import no.sigurof.plotting.BillboardResource
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import no.sigurof.plotting.ShaderManager


fun main() {
//    val matrix = matrixOfRows(
//        doubleArrayOf(0.9, 0.0, 0.0),
//        doubleArrayOf(0.0, 0.9, 0.0),
//    )
    val trainingData: List<PosVsColor> = randomlyDistributedPoints(n = 100).map { vec ->
        PosVsColor(
            pos = XY(vec.x.toDouble(), vec.y.toDouble()),
            color = if (vec.x * vec.x + vec.y * vec.y + vec.x < 0.5) "blue" else "red"
        )
    }
    plot(
        background = Vector3f(0.3f, 0.3f, 0.3f),
        data = trainingData.map {
            Circle(
                center = Vector2f(it.pos.x.toFloat(), it.pos.y.toFloat()),
                radius = 0.01,
                color = if (it.color == "blue") BLUE else RED
            )
        }
    )
    val network = NeuralNetworkBuilder(
        layers = listOf(
            2, 2
        )
    )
        .train(trainingData)
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader = Network2x2Shader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            setBackgroundColor(Vector4f(0.3f, 0.3f, 0.3f, 1f))
            shader.use()
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            shader.loadMatrix(network.weights[0])
            billboard.activate()
            billboard.render()
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




