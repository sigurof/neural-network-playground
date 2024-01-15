import axios from "axios";

export type InputVsOutput = { input: number[]; output: number[] };

export type MatrixDto = {
    rows: number;
    columns: number;
    data: number[][];
};

export type ConnectionDto = {
    inputs: number;
    outputs: number;
    weights: number;
    biases: number;
    matrix: MatrixDto;
};

export type NeuralNetworkDto = {
    layerSizes: number[];
    connections: ConnectionDto[];
};

export const serverEvents = {
    askSetModel: `no.sigurof.ml.server.web.websockets.ServerEvent.AskSetModel`,
    update: `no.sigurof.ml.server.web.websockets.ServerEvent.Update`,
    clientError: `no.sigurof.ml.server.web.websockets.ServerEvent.ClientError`,
} as const;

export type AskSetModel = {
    type: typeof serverEvents.askSetModel;
};

export type Update = {
    type: typeof serverEvents.update;
    neuralNetwork: NeuralNetworkDto;
};

export type ClientError = {
    type: typeof serverEvents.clientError;
    message: string;
};

export type ServerEvent = AskSetModel | Update | ClientError;

export type CostUpdate = {
    step: number;
    cost: number;
};

export type TrainedNeuralNetworkDto = {
    neuralNetwork: NeuralNetworkDto;
    costUpdate: CostUpdate[];
};

export type ModelDto = {
    hiddenLayers: number[];
    sizeDataSet: number;
    sizeTestSet: number;
};

export type SessionDto = {
    id: string;
    progress: number;
    result?: NeuralNetworkDto;
    model: ModelDto;
};

// Rewrite of api object as a class:
class Api {
    // constructor takes base url as argument
    constructor(private baseUrl: string) {}

    async train({
        trainingData,
        hiddenLayerDimensions,
    }: {
        trainingData: InputVsOutput[];
        hiddenLayerDimensions: number[];
    }): Promise<TrainedNeuralNetworkDto> {
        const promise = await axios.post(
            `${this.baseUrl}/ml/network`,
            {
                trainingData: trainingData,
                hiddenLayerDimensions: hiddenLayerDimensions,
            },
            {
                headers: {
                    "Content-Type": "application/json",
                },
            },
        );
        return promise.data;
    }

    async getSessions(): Promise<SessionDto[]> {
        const res = await axios.get(`${this.baseUrl}/ml/sessions`);
        return await res.data;
    }

    async getTestData(numTest: number) {
        const res = await axios.get(`${this.baseUrl}/ml/mnist/testData`, {
            params: {
                size: numTest,
            },
        });
        return res.data;
    }
}

export const api = new Api("http://localhost:8080");
