import { connectionOfData, NeuralNetwork } from "./neural-network.ts";

describe("Neural Network", () => {
    it("works", () => {
        const neuralNetwork = new NeuralNetwork({
            connections: [
                connectionOfData([
                    [0, 0, 0],
                    [0, 0, 0],
                ]),
            ],
            layerSizes: [2, 2],
        });
        expect(neuralNetwork.evaluateActivations([0, 0])).toEqual([
            [0, 0],
            [0.5, 0.5],
        ]);
    });

    it("works2", () => {
        const neuralNetwork = new NeuralNetwork({
            connections: [
                connectionOfData([
                    [0, 0, 100],
                    [0, 0, 100],
                ]),
            ],
            layerSizes: [2, 2],
        });
        expect(neuralNetwork.evaluateOutput([0, 0])).toEqual([expect.closeTo(1, 3), expect.closeTo(1, 3)]);
    });
});
