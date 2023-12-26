package org.example

import org.joml.Vector2f

const val CENTER = "center"
const val RADIUS = "radius"
const val ASPECT_RATIO = "aspectRatio"

class SphereShader : Shader(
    vtxSource = "/shader/simple/vertex.shader",
    frgSource = "/shader/simple/fragment.shader",
    attributes = emptyList(),
    uniforms = listOf(
        CENTER,
        RADIUS,
        ASPECT_RATIO
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
}
