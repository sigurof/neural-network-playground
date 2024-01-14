package no.sigurof.ml.utils

import kotlinx.serialization.Serializable

@Serializable
class Matrix(val rows: Int, val data: DoubleArray) {
    init {
        require(
            data.size % rows == 0
        ) {
            """
            Failed to initialize matrix. The number of elements per row must be an
            integer but was ${data.size}/$rows = ${data.size.toDouble() / rows}.
            """.trimIndent()
        }
    }

    val cols = data.size / rows

    constructor(rows: Int, cols: Int, function: (row: Int, col: Int) -> Double) : this(
        rows,
        DoubleArray(rows * cols) { i -> function.invoke(i / rows, i % cols) }
    )

    constructor(rows: Int, cols: Int) : this(rows, cols, { _, _ -> 0.0 })

    operator fun get(row: Int): DoubleArray {
        return data.copyOfRange(row * cols, (row + 1) * cols)
    }

    operator fun get(
        row: Int,
        col: Int,
    ): Double = data[row * cols + col]

    operator fun set(
        row: Int,
        col: Int,
        value: Double,
    ) {
        data[row * cols + col] = value
    }

    operator fun times(other: DoubleArray): DoubleArray {
        require(
            other.size == cols
        ) { "Attempted to multiply a ${rows}x$cols matrix by a ${other.size}x1 column matrix." }
        val result = DoubleArray(rows)
        for (row in 0 until rows) {
            var sum = 0.0
            for (col in 0 until cols) {
                sum += this[row, col] * other[col]
            }
            result[row] = sum
        }
        return result
    }

    operator fun times(other: Matrix): Matrix {
        val result = Matrix(rows, other.cols)
        for (row in 0 until rows) {
            for (col in 0 until other.cols) {
                var sum = 0.0
                for (i in 0 until cols) {
                    sum += this[row, i] * other[i, col]
                }
                result[row, col] = sum
            }
        }
        return result
    }

    fun plusRow(matrixRow: DoubleArray): Matrix {
        require(
            matrixRow.size == cols
        ) {
            "Failed to add row. The number of elements in the row must be " +
                "equal to the number of columns in the matrix. ${matrixRow.size} != $cols"
        }
        val newNumRows = this.rows + 1
        val newData = data.copyOf(cols * newNumRows)
        matrixRow.forEachIndexed { index, value ->
            newData[(newNumRows - 1) * cols + index] = value
        }
        return Matrix(
            rows = newNumRows,
            data = newData
        )
    }
}

fun matrixOfRows(vararg rows: DoubleArray): Matrix =
    Matrix(
        rows = rows.size,
        data =
            rows.flatMap {
                it.asIterable()
            }.toDoubleArray()
    )

fun matrixRow(vararg values: Double): DoubleArray = values
