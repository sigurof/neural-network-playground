package no.sigurof.no.sigurof.plotting.engine

import DisplayManager
import DisplayManager.Companion.HEIGHT
import DisplayManager.Companion.WIDTH
import no.sigurof.no.sigurof.plotting.BillboardManager
import no.sigurof.no.sigurof.plotting.Circle
import no.sigurof.no.sigurof.plotting.SphereShader
import no.sigurof.plotting.BillboardResource
import no.sigurof.plotting.ShaderManager
import org.joml.Vector4f
import org.lwjgl.opengl.GL30

object CoreEngine {

    private fun clearScreen(x: Float, y: Float, z: Float, w: Float) {
        GL30.glClearColor(x, y, z, w)
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
    }

    fun play(points: List<Circle>, background: Vector4f) {
        DisplayManager.FPS = 60
        DisplayManager.withWindowOpen { window ->
            val shader = SphereShader()
            val billboard: BillboardResource = BillboardManager.getBillboardResource()
            DisplayManager.eachFrameDo {
                clearScreen(background.x, background.y, background.z, background.w)
                shader.use()
                shader.loadAspectRatio(WIDTH.toFloat() / HEIGHT.toFloat())
                billboard.activate()
                for (point in points) {
                    shader.loadCenter(point.center)
                    shader.loadColor(point.color)
                    shader.loadRadius(point.radius.toFloat())
                    billboard.render()
                }
                billboard.deactivate()
            }
            ShaderManager.cleanUp()
        }

    }

}
