package org.example

class Matrix(val rows: Int, val data: DoubleArray) {
    init {
        require(
            data.size % rows == 0,
            { "Failed to initialize matrix. The number of elements per row must be an integer but was ${data.size}/${rows} = ${data.size.toDouble() / rows}." })
    }

    val cols = data.size / rows;

    constructor(rows: Int, cols: Int, function: (row: Int, col: Int) -> Double) : this(
        rows,
        DoubleArray(rows * cols) { i -> function.invoke(i / rows, i % cols) })

    constructor(rows: Int, cols: Int) : this(rows, cols, { _, _ -> 0.0 })

    operator fun get(row: Int): DoubleArray {
        return data.copyOfRange(row * cols, (row + 1) * cols)
    }

    operator fun get(row: Int, col: Int): Double {
        return data[row * cols + col]
    }

    operator fun set(row: Int, col: Int, value: Double) {
        data[row * cols + col] = value
    }

    operator fun times(other: Array1): Array1 {
        val result = Array1(rows)
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
}

fun randomArray2(rows: Int, cols: Int): Matrix {
    val array2 = Matrix(rows, cols)
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            array2[row, col] = Math.random()
        }
    }
    return array2
}

fun matrixOfRows(vararg rows: DoubleArray): Matrix {
    return Matrix(rows = rows.size, data = rows.flatMap { it.asIterable() }.toDoubleArray())
}

fun matrixRow(vararg values: Double): DoubleArray {
    return values
}
