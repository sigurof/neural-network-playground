package no.sigurof.no.sigurof.plotting

import no.sigurof.no.sigurof.plotting.engine.CoreEngine
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f




open abstract class Shape {

}

class Circle(
    val center: Vector2f,
    val radius: Double,
    val color: Vector3f,
) : Shape() {

}


fun plot(data: List<Circle>, background: Vector3f) {
    CoreEngine.play(data, background = Vector4f(background, 1f))
}
