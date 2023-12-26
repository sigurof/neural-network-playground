package no.sigurof.no.sigurof.plotting

import no.sigurof.plotting.Shader
import no.sigurof.plotting.ShaderManager
import org.joml.Vector2f
import org.joml.Vector3f

const val CENTER = "center"
const val RADIUS = "radius"
const val ASPECT_RATIO = "aspectRatio"
const val COLOR = "color"

class SphereShader : Shader(
    vtxSource = "/shader/simple/vertex.shader",
    frgSource = "/shader/simple/fragment.shader",
    attributes = emptyList(),
    uniforms = listOf(
        CENTER,
        RADIUS,
        ASPECT_RATIO,
        COLOR
    )
) {
    fun loadCenter(point: Vector2f) {
        ShaderManager.loadVector2(locations.getValue(CENTER), point)
    }

    fun loadRadius(fl: Float) {
        ShaderManager.loadFloat(locations.getValue(RADIUS), fl)
    }

    fun loadAspectRatio(fl: Float) {
        ShaderManager.loadFloat(locations.getValue(ASPECT_RATIO), fl)
    }

    fun loadColor(color: Vector3f) {
        ShaderManager.loadVector3(locations.getValue(COLOR), color)
    }
}
