import { range } from "../utils/utils.ts";
import { MatrixDto, NeuralNetworkDto } from "../../api/api.ts";

function matrixMultiplication(data: number[][], param2: number[]): number[] {
    const outputLength = data.length;
    const output = range(outputLength).map(() => 0);
    for (let iRow = 0; iRow < data.length; iRow++) {
        let outputRow = 0;
        for (let iCol = 0; iCol < data[iRow].length; iCol++) {
            outputRow += param2[iCol] * data[iRow][iCol];
        }
    }
    return output;
}

function sigmoid(x: number): number {
    return 1 / (1 + Math.exp(-x));
}

function elementwiseSigmoid(numbers: number[]): number[] {
    return numbers.map((it) => sigmoid(it));
}

export class NeuralNetwork {
    constructor(public d: NeuralNetworkDto) {}

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

    evaluateCost(testingData: { in: number[]; out: number[] }[]) {
        return testingData
            .map((testExample) => {
                const activations = this.evaluateActivations(testExample.in);
                const outputActivations = activations[activations.length - 1];
                const squaredError = testExample.out.map((expectedOutput, i) => {
                    const actualOutput = outputActivations[i];
                    return (expectedOutput - actualOutput) ** 2;
                });
                return squaredError.reduce((a, b) => a + b, 0);
            })
            .reduce((a, b) => a + b, 0);
    }
}

