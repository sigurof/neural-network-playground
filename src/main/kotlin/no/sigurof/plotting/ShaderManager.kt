package no.sigurof.plotting

import java.nio.FloatBuffer
import org.joml.Matrix3f
import org.joml.Matrix3x2fc
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil

public object ShaderManager {

    private val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)

    var activeShaders: MutableSet<Shader> = mutableSetOf()
        private set

    fun cleanUp() {
        activeShaders.forEach { it.cleanUp() }
    }

    fun getUniformLocation(uniformName: String, program: Int): Int {
        return GL20.glGetUniformLocation(program, uniformName)
            .takeIf { it != -1 }
            ?: error("Uniform $uniformName does not have an active location.")
    }

    fun compileProgram(vtxSource: String, frgSource: String, attributes: List<Pair<Int, String>>): Int {
        val program = GL20.glCreateProgram()
        val vtxShader: Int =
            compileShaderFromSource(
                vtxSource,
                GL20.GL_VERTEX_SHADER
            )
        val frgShader: Int =
            compileShaderFromSource(
                frgSource,
                GL20.GL_FRAGMENT_SHADER
            )
        GL20.glAttachShader(program, vtxShader)
        GL20.glAttachShader(program, frgShader)
        for (attribute in attributes) {
            bindAttribute(attribute.first, attribute.second, program)
        }
        GL20.glLinkProgram(program)
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            val info = GL20.glGetProgramInfoLog(program, 512)
            throw RuntimeException("Feil ved linking av shadere:\n $info")
        }
        GL20.glDetachShader(program, vtxShader)
        GL20.glDetachShader(program, frgShader)
        GL20.glDeleteShader(vtxShader)
        GL20.glDeleteShader(frgShader)
        return program
    }

    private fun compileShaderFromSource(source: String, typeGl: Int): Int {
        val text = ShaderManager::class.java.getResource(source).readText()
        val shader = GL20.glCreateShader(typeGl)
        GL20.glShaderSource(shader, text)
        GL20.glCompileShader(shader)
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            val info = GL20.glGetShaderInfoLog(shader, 512)
            val shaderName = when (typeGl) {
                GL20.GL_VERTEX_SHADER -> "vertex"
                GL20.GL_FRAGMENT_SHADER -> "fragment"
                else -> "ukjent"
            }
            throw RuntimeException("Error when compiling $shaderName shader:\n $info")
        }
        return shader
    }

    private fun bindAttribute(attributeIdx: Int, variableName: String, program: Int) {
        GL20.glBindAttribLocation(program, attributeIdx, variableName)
    }

    fun loadFloat(location: Int, value: Float) {
        GL20.glUniform1f(location, value)
    }

    fun loadVector3(location: Int, vector: Vector3f) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z)
    }

    fun loadVector2(location: Int, vector: Vector2f) {
        GL20.glUniform2f(location, vector.x, vector.y)
    }

    fun loadBoolean(location: Int, value: Boolean) {
        GL20.glUniform1f(
            location, when (value) {
                true -> 1f
                false -> 0f
            }
        )
    }

    fun loadMatrix(location: Int, matrix4f: Matrix4f) {
        matrix4f.get(matrixBuffer)
        GL20.glUniformMatrix4fv(location, false, matrixBuffer)
    }

    fun loadMatrix3x3(location: Int, matrix3x3: FloatArray) {
        val floatBuffer: FloatBuffer = MemoryUtil.memAllocFloat(9).put(matrix3x3).flip()
        GL20.glUniformMatrix3fv(location, false, floatBuffer)
        MemoryUtil.memFree(floatBuffer)
    }

    fun loadInt(location: Int, value: Int) {
        GL20.glUniform1i(location, value)
    }

}
