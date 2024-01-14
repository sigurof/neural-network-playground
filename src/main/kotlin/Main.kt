package no.sigurof

import no.sigurof.no.sigurof.plotting.BLUE
import no.sigurof.no.sigurof.plotting.Circle
import no.sigurof.no.sigurof.plotting.RED
import no.sigurof.no.sigurof.plotting.plot
import no.sigurof.no.sigurof.randomlyDistributedPoints
import org.joml.Vector2f
import org.joml.Vector3f


fun main() {
    val circles = randomlyDistributedPoints(100).map { vec ->
        val x = vec.x
        val y = vec.y
        val color = if (x > 0 && y > 0) BLUE else RED
        Circle(center = Vector2f(x, y), radius = 0.01, color = color)
    }
    plot(
        background = Vector3f(0.3f, 0.3f, 0.3f),
        data = circles
    )




}




