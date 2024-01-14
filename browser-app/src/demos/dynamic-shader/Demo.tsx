import { useEffect, useMemo, useRef, useState } from "react";
import axios from "axios";
import { CircleData, startThree, threeJsInitialized } from "./ThreeCode.ts";
import {
    circlesDataSets as circlesDataSets2,
    MLInputOutput,
    TrainingData,
} from "./data.ts";
import { Slider } from "@mui/material";
import styled from "styled-components";
import { chartInitialized, startChartJs } from "./ChartJsCode.ts";
import { range } from "../../utils.ts";
import ColorGrid, { RGBColor } from "./ColorGrid.tsx";

export type Matrix = {
    rows: number;
    columns: number;
    data: number[][];
};

const mlTrainingData: MLInputOutput[] =
    circlesDataSets2.nonLinear.circularRegion;
const circleData: CircleData[] = mlTrainingData.map((data) => {
    return {
        pos: {
            x: data.input[0],
            y: data.input[1],
        },
        color: {
            r: data.output[0],
            g: 0,
            b: data.output[1],
        },
    };
});
const hiddenLayerDimensions: number[] = [4, 4];
const layerDimensions = [
    mlTrainingData[0].input.length,
    ...hiddenLayerDimensions,
    mlTrainingData[0].output.length,
];
const zipWithNext = (arr: number[]) => {
    const result: { left: number; right: number }[] = [];
    for (let i = 0; i < arr.length - 1; i++) {
        result.push({ left: arr[i], right: arr[i + 1] });
    }
    return result;
};

function calculateWeightsAndBiasesDimensions(
    inputNodes: number,
    outputNodes: number,
) {
    const numberOfBiases = outputNodes;
    const numberOfWeights = inputNodes * outputNodes;
    return numberOfWeights + numberOfBiases;
}

export type NetworkConnection = {
    rows: number;
    columns: number;
};
const weightsAndBiasesDimensions: NetworkConnection[] = zipWithNext(
    layerDimensions,
).map(({ left, right }) => {
    return {
        rows: right,
        columns: left + 1, // +1 for the bias
    };
});

const initialState: Matrix[] = weightsAndBiasesDimensions.map((dimensions) => {
    const { rows, columns } = dimensions;
    const data = range(rows).map(() => range(columns).map(() => 0.0));
    return {
        rows: rows,
        columns: columns,
        data: data,
    };
});

async function train(
    trainingData: TrainingData,
    hiddenLayerDimensions: number[],
): Promise<unknown> {
    const res = await axios.post(
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
    );
    return res.data;
}

function castToStats(result: unknown): { step: number; cost: number }[] {
    const record: unknown = (result as { record: unknown }).record;
    return record as { step: number; cost: number }[];
}

function castToSimpleNetworkLayer(data: unknown): Matrix[] {
    const layers = (data as { layers: unknown }).layers;

    // throw error if not an array:
    if (!Array.isArray(layers)) {
        throw new Error("data is not an array");
    }
    // throw error if the array is empty:
    if (layers.length < 1) {
        throw new Error("data is not of length 1");
    }

    return layers;
}

const MyLabel = styled.label`
    grid-column-start: 1;
`;
const SliderContainer = styled.div`
    display: grid;
    grid-template-columns: 1fr 2fr;
`;

const WeightBiasInput = ({
    type,
    name,
    value,
    handleChange,
}: {
    type: "weight" | "bias";
    name: string;
    value: number;
    handleChange: (value: number) => void;
}) => {
    return (
        <SliderContainer>
            <MyLabel htmlFor={"input" + name}>{`${type}`}</MyLabel>
            <Slider
                id={"input" + name}
                value={value}
                size={"small"}
                min={-10}
                max={10}
                step={0.01}
                onChange={(_, newValue) => {
                    const valueAsNumber = Number(newValue);
                    if (isNaN(valueAsNumber)) {
                        return;
                    }
                    handleChange(newValue as number);
                }}
                aria-label="Default"
                valueLabelDisplay="auto"
            />
        </SliderContainer>
    );
};
const MatrixRowWrapper = styled.div`
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    column-gap: 1rem;
`;

const MatrixRowInput = ({
    name,
    row,
    handleChange,
}: {
    name: string;
    row: number[];
    handleChange: (value: number[]) => void;
}) => {
    return (
        <MatrixRowWrapper>
            {row.map((value, index) => {
                const isLastOnRow = index === row.length - 1;
                return (
                    <WeightBiasInput
                        type={isLastOnRow ? "bias" : "weight"}
                        key={`input${name}${index}`}
                        name={`input${name}${index}`}
                        value={value}
                        handleChange={(newValue: number) => {
                            const newValues = [...row];
                            newValues[index] = newValue;
                            handleChange(newValues);
                        }}
                    />
                );
            })}
        </MatrixRowWrapper>
    );
};

function InputFieldsForMatrix({
    matrixIndex,
    handleFormChange,
    value,
}: {
    matrixIndex: number;
    handleFormChange: (form: Matrix) => void;
    value: Matrix;
}) {
    return (
        <div>
            <h3>Matrix {matrixIndex}</h3>
            {value.data.map((row: number[], rowIndex: number) => {
                return (
                    <MatrixRowInput
                        key={`matrix${matrixIndex}row${rowIndex}`}
                        name={`matrix${matrixIndex}row${rowIndex}`}
                        row={row}
                        handleChange={(newValue: number[]) => {
                            const newMatrixData = [...value.data];
                            newMatrixData[rowIndex] = newValue;
                            const newMatrix = {
                                ...value,
                                data: newMatrixData,
                            };
                            handleFormChange(newMatrix);
                        }}
                    />
                );
            })}
        </div>
    );
}

const GraphicsContainer = styled.div`
    display: grid;
    grid-template-columns: 1fr 1fr;
    column-gap: 1rem;
`;

const ThreeJsContainer = styled.canvas`
    grid-column-start: 1;
`;

const ChartContainer = styled.div`
    grid-column-start: 2;
`;

function elementwiseSigmoid(numbers: number[]) {
    return numbers.map((number) => {
        return 1 / (1 + Math.exp(-number));
    });
}

function matrixMultiplication(matrix: Matrix, vector: number[]) {
    const result: number[] = [];
    for (let row = 0; row < matrix.rows; row++) {
        let sum = 0;
        for (let col = 0; col < matrix.columns; col++) {
            sum += matrix.data[row][col] * vector[col];
        }
        result.push(sum);
    }
    return result;
}

function evaluateNetwork(row: number, col: number, form: Matrix[]): RGBColor {
    const activations = [[row, col]];
    let lastActivations = 0;
    for (const matrix of form) {
        activations.push(
            elementwiseSigmoid(
                matrixMultiplication(matrix, activations[lastActivations]),
            ),
        );
        lastActivations++;
    }
    const activation = activations[activations.length - 1];
    // check that activation has length 2
    if (activation.length !== 2) {
        throw new Error("activation does not have length 2");
    }
    return [
        Math.round(activation[0] * 255),
        0,
        Math.round(activation[1] * 255),
    ];
}

async function askBackendForImage(form: Matrix[]) {
    // Call backend ml/evaluate endpoint with the form variable as the body
    // Receive a png image byte stream
    const response = await axios.post(
        "http://localhost:8080/ml/evaluate",
        form,
        {
            responseType: "blob",
        },
    );
    return URL.createObjectURL(response.data);
}

export const Demo = () => {
    const [form, setForm] = useState<Matrix[]>(initialState);
    const threeJsController = useRef<{
        update: (form: Matrix[]) => void;
        tearDown: () => void;
    } | null>(null);
    const chartUpdater = useRef<{
        update: (points: { x: number; y: number }[]) => void;
    } | null>(null);

    function handleFormChange(form: Matrix[]) {
        setForm(form);
        threeJsController.current!.update(form);
    }
    const newRandom = Math.random();
    console.log(`The new random is ${newRandom}`)

    // a useeffect calling threeJsController.tearDown on unmount
    useEffect(() => {
        return () => {
            console.log(`tearing down ${newRandom}}`);
            threeJsController.current?.tearDown();
        };
    }, []);

    // useEffect(() => {
    //     if (!chartInitialized) {
    //         chartUpdater.current = {
    //             update: startChartJs()!.updateChart,
    //         };
    //     }
    // }, []);
    useEffect(() => {
        if (!threeJsInitialized) {
            const result = startThree(
                form,
                circleData,
                weightsAndBiasesDimensions,
            );
            if (result) {
                threeJsController.current = {
                    update: result.update,
                    tearDown: result.tearDown,
                };
            }
        }
    }, []);
    const numPixelsX = 10;
    const numPixelsY = 10;

    // const colors = useMemo(, [form]);

    const [imageSrc, setImageSrc] = useState("");
    return (
        <>
            {form.map((matrix, index) => {
                return (
                    <InputFieldsForMatrix
                        key={`matrix${index}`}
                        matrixIndex={index}
                        value={matrix}
                        handleFormChange={(newMatrix: Matrix) => {
                            const newMatrices = [...form];
                            newMatrices[index] = newMatrix;
                            handleFormChange(newMatrices);
                        }}
                    />
                );
            })}
            <button
                onClick={async () => {
                    const result = await train(
                        mlTrainingData,
                        hiddenLayerDimensions,
                    );
                    const formData = castToSimpleNetworkLayer(result);
                    const statistics = castToStats(result);
                    handleFormChange(formData);
                    chartUpdater.current!.update(
                        statistics.map((stat) => {
                            return { x: stat.step, y: stat.cost };
                        }),
                    );
                }}
            >
                Train!
            </button>
            <GraphicsContainer>
                <ThreeJsContainer id="threeCanvas" />

                {/*<ChartContainer>*/}
                {/*    <canvas id="chartCanvas" />*/}
                {/*</ChartContainer>*/}
            </GraphicsContainer>
            <button
                onClick={async () => {
                    const url = await askBackendForImage(form);
                    setImageSrc(url);
                }}
            >
                Reset
            </button>
            Show a 500 x 500 img
            {imageSrc && <img src={imageSrc} width={500} height={500} />}
        </>
    );
};
