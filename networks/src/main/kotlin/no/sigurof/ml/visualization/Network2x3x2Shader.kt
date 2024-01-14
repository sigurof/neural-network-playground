package no.sigurof.ml.visualization

import no.sigurof.ml.Matrix
import no.sigurof.plotting.Shader
import no.sigurof.plotting.ShaderManager

private const val ASPECT_RATIO_H = "aspectRatio"
private const val FIRST_WEIGHTS = "firstWeights"
private const val SECOND_WEIGHTS = "secondWeights"

class Network2x3x2Shader : Shader(
    vtxSource = "/shader/neural-network/2x3x2/vertex.shader",
    frgSource = "/shader/neural-network/2x3x2/fragment.shader",
    attributes = emptyList(),
    uniforms = listOf(
        ASPECT_RATIO_H,
        FIRST_WEIGHTS,
        SECOND_WEIGHTS
    )
) {
    fun loadAspectRatio(fl: Float) {
        ShaderManager.loadFloat(locations.getValue(ASPECT_RATIO_H), fl)
    }

    fun loadFirstWeights(matrix3x3: Matrix) {
        ShaderManager.loadMatrix3(
            locations.getValue(FIRST_WEIGHTS),
            matrix3x3.data.map { it.toFloat() }.toFloatArray()
        )
    }

    fun loadSecondWeights(matrix2x4: Matrix) {
        val floatArray: FloatArray = matrix2x4
            .plusRow(doubleArrayOf(0.0, 0.0, 0.0, 0.0))
            .plusRow(doubleArrayOf(0.0, 0.0, 0.0, 0.0))
            .data.map { it.toFloat() }.toFloatArray()
        ShaderManager.loadMatrix4(
            locations.getValue(SECOND_WEIGHTS),
            floatArray
        )
    }
}
