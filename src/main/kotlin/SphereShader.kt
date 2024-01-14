package org.example

import org.joml.Vector2f

const val CENTER = "center"
const val RADIUS = "radius"

class SphereShader : Shader(
    vtxSource = "/shader/simple/vertex.shader",
    frgSource = "/shader/simple/fragment.shader",
    attributes = emptyList(),
    uniforms = listOf(
        CENTER,
        RADIUS
    )
) {
    fun loadCenter(point: Vector2f) {
        ShaderManager.loadVector2(locations.getValue(CENTER), point)
    }

    fun loadRadius(fl: Float) {
        ShaderManager.loadFloat(locations.getValue(RADIUS), fl)
    }
}
