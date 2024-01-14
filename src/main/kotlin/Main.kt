package org.example

import DisplayManager
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

open class Data<Value, Label>(
    val value: Value,
    val label: Label,
)

class PosVsColor(
    pos: XY,
    color: String,
) : Data<XY, String>(pos, color)

typealias Array1 = DoubleArray

var WIDTH: Int = 1280
var HEIGHT: Int = 720
fun resizeWindow(width: Int, height: Int) {
    WIDTH = width
    HEIGHT = height
    GL20.glViewport(0, 0, WIDTH, HEIGHT)
}

private fun windowResizeCallback(w: Long, width: Int, height: Int) {
    resizeWindow(width, height)
}

private fun clearScreen(x: Float, y: Float, z: Float, w: Float) {
    GL30.glClearColor(x, y, z, w)
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
}

private var lastUpdate: Long = System.currentTimeMillis()

private fun clearScreen(background: Vector4f) {
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
    GL30.glClearColor(background.x, background.y, background.z, background.w)
}

var FPS = 120
fun main() {
    val points: List<Vector2f> = listOf(
        // a few randomly scattered points
        Vector2f(-0.5f, 0.5f),
        Vector2f(-0.25f, -0.15f),
        Vector2f(0.5f, 0.5f),
        Vector2f(0.75f, -0.5f),
        Vector2f(0f, 0f)

    )
    DisplayManager.FPS = 60
    DisplayManager.withWindowOpen { window ->
        val shader: SphereShader = SphereShader()
        val billboard: BillboardResource = BillboardManager.getBillboardResource()
        DisplayManager.eachFrameDo {
            clearScreen(0f, 0f, 0f, 0f)
            shader.use()
            shader.loadRadius(0.1f)
            for (point in points) {
                billboard.activate()
                shader.loadCenter(point)
                billboard.render()
                billboard.deactivate()
            }
        }
        ShaderManager.cleanUp()
    }


}

fun trainingNeuralNetworkAndPngs() {
//    val colorsList = blueCircleInRedBackgroundColorsList(100, 100);
//    createPngFromColorsList(colorsList)
    val trainingData: List<PosVsColor> = leftRedRightBlue()
    val neuralNetwork = NeuralNetworkBuilder(
        layers = listOf(2, 2)
    ).train(trainingData)
//    val output = neuralNetwork.evaluate(XY(1.0, 1.0))
    pngFromColorPicker(
        pixelWidth = 10,
        pixelHeight = 10,
        fileName = "./expected.png"
    ) { row, col ->
        val startX = -1.0
        val startY = -1.0
        val endX = 1.0
        val endY = 1.0
        val stepX = (endX - startX) / (10 - 1)
        val stepY = (endY - startY) / (10 - 1)
        val x = startX + col * stepX
        val y = startY + row * stepY
        val color = if (x * x + y * y < 1) Rgb(0, 0, 255) else Rgb(255, 0, 0)
        Color(color.r, color.g, color.b)
    }
    pngFromColorPicker(
        pixelWidth = 1000,
        pixelHeight = 1000,
        fileName = "./actual.png"
    ) { row, col ->
        val startX = -10.0
        val startY = -10.0
        val endX = 10.0
        val endY = 10.0
        val stepX = (endX - startX) / (1000 - 1)
        val stepY = (endY - startY) / (1000 - 1)
        val x = startX + col * stepX
        val y = startY + (1000 - row) * stepY
        val colorString: String =
            neuralNetwork.evaluate(x, y).entries.maxByOrNull { it.value }!!.key
        val color = if (colorString == "red") {
            Rgb(255, 0, 0)
        } else {
            Rgb(0, 0, 255)
        }
        Color(color.r, color.g, color.b)
    }
}

fun leftRedRightBlue(): List<PosVsColor> {
    val trainingData: MutableList<PosVsColor> = mutableListOf()
    for (i in 0 until 100) {
        val x = -1.0 + i * 0.02
        for (j in 0 until 100) {
            val y = -1.0 + j * 0.02
            val color = if (x < -0.3 && y < 0) "red" else "blue"
            trainingData.add(PosVsColor(XY(x, y), color))
        }
    }
    return trainingData
}

fun pngFromColorPicker(
    pixelWidth: Int,
    pixelHeight: Int,
    fileName: String,
    neuralNetwork: (row: Int, col: Int) -> Color,
) {

    val image = BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB)
    for (col in 0 until pixelWidth) {
        for (row in 0 until pixelHeight) {
            image.setRGB(col, row, neuralNetwork.invoke(row, col).rgb)
        }
    }
    ImageIO.write(image, "png", File(fileName))

}


fun blueCircleInRedBackgroundColorsList(width: Int, height: Int): List<List<Rgb>> {
    val colors = mutableListOf<List<Rgb>>()
    for (pixelX in 0 until width) {
        val row = mutableListOf<Rgb>()
        val x = pixelX.toDouble() / width
        for (pixelY in 0 until height) {
            val y = pixelY.toDouble() / height
            val color = if (x * x + y * y < 1) Rgb(0, 0, 255) else Rgb(255, 0, 0)
            row.add(color)
        }
        colors.add(row)
    }
    return colors
}

data class Rgb(
    val r: Int,
    val g: Int,
    val b: Int,
)


fun createPngFromColorsList(colors: List<List<Rgb>>) {
    val height = colors.size
    val width = colors[0].size
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val color = colors[x][y]
            image.setRGB(x, y, Color(color.r, color.g, color.b).rgb)
        }
    }
    val outputfile = File("./image.png")
    ImageIO.write(image, "png", outputfile)

}

fun blueCircleInRedBackground(): List<PosVsColor> {
    // create a 10 x 10 grid of points between -1 and 1
    val stepX = 2.0 / 9
    val grid = mutableListOf<XY>()
    for (i in 0 until 10) {
        val x = -1.0 + i * stepX;
        for (j in 0 until 10) {
            val y = -1.0 + j * stepX;
            grid.add(XY(x, y))
        }
    }
    // label each point as red or blue depending on whether it is inside the unit circle
    val trainingData: MutableList<PosVsColor> = mutableListOf()
    for (xy in grid) {
        val color = if (xy.x * xy.x + xy.y * xy.y < 1) "blue" else "red"
        trainingData.add(PosVsColor(xy, color))
    }
    return trainingData
}



