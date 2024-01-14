package no.sigurof.no.sigurof

import no.sigurof.no.sigurof.plotting.BLUE
import no.sigurof.no.sigurof.plotting.Circle
import no.sigurof.no.sigurof.plotting.RED
import org.joml.Vector2f


fun randomlyDistributedPoints(n: Int): MutableList<Vector2f> {
    val points = mutableListOf<Vector2f>()
    for (i in 0 until n) {
        val x = -1.0f + 2.0f * Math.random().toFloat()
        val y = -1.0f + 2.0f * Math.random().toFloat()
        points.add(Vector2f(x, y))
    }
    return points
}

fun gridPoints(nRows: Int, nCols: Int): MutableList<Vector2f> {
    val startX = -1.0f
    val startY = -1.0f
    val endX = 1.0f
    val endY = 1.0f
    val stepX = (endX - startX) / (nCols - 1)
    val stepY = (endY - startY) / (nRows - 1)
    val grid = mutableListOf<Vector2f>()
    for (i in 0 until nCols) {
        val x = startX + i * stepX;
        for (j in 0 until nRows) {
            val y = startY + j * stepY;
            grid.add(Vector2f(x, y))
        }
    }
    return grid
}

fun convertToBlueUnitCircle(grid: MutableList<Vector2f>, radius: Double): List<Circle> {
    return grid.map {
        val color = if (it.x * it.x + it.y * it.y < 1) BLUE else RED
        val center = Vector2f(
            it.x,
            it.y
        )
        Circle(center = center, radius = radius, color = color)
    }
}

