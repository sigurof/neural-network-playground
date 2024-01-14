import { range } from "../utils/utils.ts";
import { MatrixDto } from "../../api/api.ts";

export function identityMatrix(size: number): number[][] {
    const output = range(size).map(() => range(size).map(() => 0));
    for (let i = 0; i < size; i++) {
        output[i][i] = 1;
    }
    return output;
}


export function matrixOf(data: number[][]): MatrixDto {
    // validate data
    if (data.length === 0) {
        throw new Error("data must have at least one row");
    }
    if (data[0].length === 0) {
        throw new Error("data must have at least one column");
    }
    // all rows have same length
    const rowLength = data[0].length;
    if (!data.every((row) => row.length === rowLength)) {
        throw new Error("all rows must have the same length");
    }
    return {
        rows: data.length,
        columns: data[0].length,
        data,
    };

}

export function matrixMultiplication(data: number[][], param2: number[]): number[] {
    // validate inputs
    if (data.length === 0) {
        throw new Error("data must have at least one row");
    }
    if (data[0].length === 0) {
        throw new Error("data must have at least one column");
    }
    if (data[0].length !== param2.length) {
        throw new Error("data and param2 must have the same number of columns");
    }
    return data.map((row) => row.reduce((acc, val, i) => acc + val * param2[i], 0));
}
