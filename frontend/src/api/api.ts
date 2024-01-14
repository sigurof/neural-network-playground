import axios from "axios";

export type InputOutput = { input: number[]; output: number[] };

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
} & NeuralNetworkDto;

export type ClientError = {
    type: typeof serverEvents.clientError;
    message: string;
};

export type ServerEvent = AskSetModel | Update | ClientError;


export type Record = {
    step: number;
    cost: number;
};

export type TrainedNeuralNetworkDto = {
    neuralNetwork: NeuralNetworkDto;
    record: Record[];
};

export const api = {
    train: async ({
        trainingData,
        hiddenLayerDimensions,
    }: {
        trainingData: InputOutput[];
        hiddenLayerDimensions: number[];
    }): Promise<TrainedNeuralNetworkDto> => {
        return axios
            .post(
                "http://localhost:8080/ml/network",
                {
                    trainingData: trainingData,
                    hiddenLayerDimensions: hiddenLayerDimensions,
                },
                {
                    headers: {
                        "Content-Type": "application/json",
                    },
                },
            )
            .then((response) => response.data);
    },
};
