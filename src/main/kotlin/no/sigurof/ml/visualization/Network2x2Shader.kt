package no.sigurof.no.sigurof.ml.visualization

import no.sigurof.ml.Matrix
import no.sigurof.plotting.Shader
import no.sigurof.plotting.ShaderManager
import org.joml.Matrix3f

const val ASPECT_RATIO = "aspectRatio"
const val MATRIX = "uMatrix"

class Network2x2Shader : Shader(
    vtxSource = "/shader/neural-network/2x2/vertex.shader",
    frgSource = "/shader/neural-network/2x2/fragment.shader",
    attributes = emptyList(),
    uniforms = listOf(
        ASPECT_RATIO,
        MATRIX
    )
) {
    fun loadAspectRatio(fl: Float) {
        ShaderManager.loadFloat(locations.getValue(ASPECT_RATIO), fl)
    }

    fun loadMatrix(matrix: Matrix) {
        val floatArray: FloatArray = matrix
            .plusRow(doubleArrayOf(0.0, 0.0, 0.0))
            .data.map { it.toFloat() }.toFloatArray()
        ShaderManager.loadMatrix3(
            locations.getValue(MATRIX),
            floatArray
        )
    }
}

fun Matrix.toJomlMatrix(): Matrix3f {
    val m = Matrix3f()
    for (i in 0 until this.rows) {
        val row = this[i]
        m.setRow(i, row[0].toFloat(), row[1].toFloat(), row[2].toFloat())
    }
    return m
}
