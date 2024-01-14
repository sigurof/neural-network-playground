import { range } from "../utils/utils.ts";
import { ConnectionDto, InputVsOutput, MatrixDto, NeuralNetworkDto } from "../../api/api.ts";
import { matrixMultiplication, matrixOf } from "../linalg/linalg.ts";
import { sigmoid } from "../maths/maths.ts";
import { Matrix3 } from "three";

function elementwiseSigmoid(numbers: number[]): number[] {
    return numbers.map((it) => sigmoid(it));
}

export function connectionOfData(matrix: number[][]): ConnectionDto {
    return connectionOfMatrix(matrixOf(matrix));
}

export function connectionOfMatrix(matrix: MatrixDto): ConnectionDto {
    return {
        matrix,
        weights: matrix.columns - 1,
        biases: matrix.rows,
        inputs: matrix.columns,
        outputs: matrix.rows,
    };
}

export class NeuralNetwork {
    constructor(private d: NeuralNetworkDto) {}

    evaluateActivations(inputActivations: number[]): number[][] {
        const activations = [inputActivations];
        for (let i = 0; i < this.d.connections.length; i++) {
            const layer = this.d.connections[i];
            const activationsOfLastLayer: number[] = activations[i];
            const arrayProduct = matrixMultiplication(layer.matrix.data, [...activationsOfLastLayer, 1]);
            activations.push(elementwiseSigmoid(arrayProduct));
        }
        return activations;
    }

    evaluateOutput(inputActivations: number[]): number[] {
        return this.evaluateActivations(inputActivations)[this.d.connections.length];
    }

    evaluateCost(testingData: InputVsOutput[]) {
        return testingData
            .map((testExample) => {
                const activations = this.evaluateActivations(testExample.input);
                const outputActivations = activations[activations.length - 1];
                const squaredError = testExample.output.map((expectedOutput, i) => {
                    const actualOutput = outputActivations[i];
                    return (expectedOutput - actualOutput) ** 2;
                });
                return squaredError.reduce((a, b) => a + b, 0);
            })
            .reduce((a, b) => a + b, 0);
    }
}
