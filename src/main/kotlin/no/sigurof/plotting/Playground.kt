package no.sigurof.plotting

import no.sigurof.no.sigurof.plotting.BillboardManager
import no.sigurof.no.sigurof.plotting.SphereShader
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

private var lastUpdate: Long = System.currentTimeMillis()

private fun clearScreen(background: Vector4f) {
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
    GL30.glClearColor(background.x, background.y, background.z, background.w)
}

private var FPS = 120

private fun resizeWindow(width: Int, height: Int) {
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

private var WIDTH: Int = 1280
private var HEIGHT: Int = 720
fun plotABunchOfCircles() {
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
            shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
            billboard.activate()
            for (point in points) {
                shader.loadCenter(point)
                billboard.render()
            }
            billboard.deactivate()
        }
        ShaderManager.cleanUp()
    }
}
