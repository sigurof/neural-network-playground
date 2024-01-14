import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import no.sigurof.ml.Matrix
import no.sigurof.ml.matrixOfRows
import no.sigurof.ml.matrixRow
import org.joml.Vector3f


class LinearAlgebraTest : FreeSpec({

    "something" - {
        1 shouldBe 1
    }

    "you can create a new matrix with an added row" - {
        val originalMatrix = Matrix(
            rows = 4, data = doubleArrayOf(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0,
                10.0, 11.0, 12.0,
            )
        )
        val newMatrix = originalMatrix.plusRow(doubleArrayOf(1.0, 2.0, 3.0))
        newMatrix.rows shouldBe 5
        newMatrix.cols shouldBe 3
        newMatrix.data shouldBe doubleArrayOf(
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0,
            10.0, 11.0, 12.0,
            1.0, 2.0, 3.0,
        )

    }


    "matrix multiplication" - {
        // A 4 x 3 matrix
        val a = Matrix(
            rows = 4, data = doubleArrayOf(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0,
                10.0, 11.0, 12.0,
            )
        )
        // A 3 x 2 matrix
        val b = Matrix(
            rows = 3, data = doubleArrayOf(
                1.0, 2.0,
                3.0, 4.0,
                5.0, 6.0,
            )
        )
        // The result should be a 4 x 2 matrix
        val result: Matrix = a * b
        result.rows shouldBe 4
        result.cols shouldBe 2
        result[0] shouldBe doubleArrayOf(22.0, 28.0)
        result[1] shouldBe doubleArrayOf(49.0, 64.0)
        result[2] shouldBe doubleArrayOf(76.0, 100.0)

    }

    "you can have a single column matrix" - {
        val matrix = Matrix(rows = 3, data = doubleArrayOf(1.0, 2.0, 3.0))
        matrix.rows shouldBe 3
        matrix.cols shouldBe 1
        matrix[0, 0] shouldBe 1.0
        matrix[1, 0] shouldBe 2.0
        matrix[2, 0] shouldBe 3.0
    }

    "you can have a single row matrix" - {
        val matrix = matrixOfRows(
            matrixRow(1.0, 2.0, 3.0)
        )
        matrix.rows shouldBe 1
        matrix.cols shouldBe 3
        matrix[0, 0] shouldBe 1.0
        matrix[0, 1] shouldBe 2.0
        matrix[0, 2] shouldBe 3.0
    }

    "matrix data is set to expected values" - {
        val matrixOfRows = matrixOfRows(
            matrixRow(1.0, 1.0),
            matrixRow(1.0, 2.0),
        )
        matrixOfRows.data shouldBe doubleArrayOf(1.0, 1.0, 1.0, 2.0)
    }

    "initializing matrix fails if does not have clean number of rows and columns" - {
        shouldThrow<IllegalArgumentException> {
            Matrix(rows = 3, data = DoubleArray(5) { i -> i.toDouble() })
        }.shouldHaveMessage("^Failed to initialize.+".toRegex())
    }

    "regex test" - {
        ".+Failed to.+".toRegex().matches("Some stuff. Failed to initialize matrix. The number ") shouldBe true
    }

})
