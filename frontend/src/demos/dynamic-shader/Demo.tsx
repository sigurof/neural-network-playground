import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { CircleData, startThree, threeJsInitialized } from "./ThreeCode.ts";
import { MLInputOutput } from "./data.ts";
import { Slider } from "@mui/material";
import styled from "styled-components";
import { chartInitialized, startChartJs } from "./ChartJsCode.ts";
import { range } from "../../common/utils/utils.ts";
import { RGBColor } from "./ColorGrid.tsx";
import { circlesDataSets } from "./data3d.ts";
import { api, MatrixDto, CostUpdate, TrainedNeuralNetworkDto } from "../../api/api.ts";

const mlTrainingData: MLInputOutput[] = circlesDataSets.abc.redAndBlue;
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
const hiddenLayerDimensions: number[] = [8, 6];
const layerDimensions = [mlTrainingData[0].input.length, ...hiddenLayerDimensions, mlTrainingData[0].output.length];
const zipWithNext = (arr: number[]) => {
    const result: { left: number; right: number }[] = [];
    for (let i = 0; i < arr.length - 1; i++) {
        result.push({ left: arr[i], right: arr[i + 1] });
    }
    return result;
};

function calculateWeightsAndBiasesDimensions(inputNodes: number, outputNodes: number) {
    const numberOfBiases = outputNodes;
    const numberOfWeights = inputNodes * outputNodes;
    return numberOfWeights + numberOfBiases;
}

export type NetworkConnection = {
    rows: number;
    columns: number;
};
const weightsAndBiasesDimensions: NetworkConnection[] = zipWithNext(layerDimensions).map(({ left, right }) => {
    return {
        rows: right,
        columns: left + 1, // +1 for the bias
    };
});

const initialState: MatrixDto[] = weightsAndBiasesDimensions.map((dimensions) => {
    const { rows, columns } = dimensions;
    const data = range(rows).map(() => range(columns).map(() => 0.0));
    return {
        rows: rows,
        columns: columns,
        data: data,
    };
});

function castToStats(result: TrainedNeuralNetworkDto): { step: number; cost: number }[] {
    const record: unknown = (result as { record: unknown }).record;
    return record as { step: number; cost: number }[];
}

function castToSimpleNetworkLayer(data: TrainedNeuralNetworkDto): MatrixDto[] {
    // const layers = (data as { layers: unknown }).layers;
    const layers = data.neuralNetwork.connections.map((connection) => {
        return connection.matrix;
    });

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
    handleFormChange: (form: MatrixDto) => void;
    value: MatrixDto;
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

function matrixMultiplication(matrix: MatrixDto, vector: number[]) {
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

function evaluateNetwork(row: number, col: number, form: MatrixDto[]): RGBColor {
    const activations = [[row, col]];
    let lastActivations = 0;
    for (const matrix of form) {
        activations.push(elementwiseSigmoid(matrixMultiplication(matrix, activations[lastActivations])));
        lastActivations++;
    }
    const activation = activations[activations.length - 1];
    // check that activation has length 2
    if (activation.length !== 2) {
        throw new Error("activation does not have length 2");
    }
    return [Math.round(activation[0] * 255), 0, Math.round(activation[1] * 255)];
}

async function askBackendForImage(form: MatrixDto[]) {
    // Call backend ml/evaluate endpoint with the form variable as the body
    // Receive a png image byte stream
    const response = await axios.post("http://localhost:8080/ml/evaluate", form, {
        responseType: "blob",
    });
    return URL.createObjectURL(response.data);
}

const useCharts = () => {
    const chartUpdater = useRef<{
        updateChart: (points: { x: number; y: number }[]) => void;
        destroy: () => void;
    } | null>(null);
    useEffect(() => {
        return () => {
            chartUpdater.current?.destroy();
        };
    }, []);

    useEffect(() => {
        if (!chartInitialized) {
            const startChartJs1 = startChartJs();
            chartUpdater.current = {
                ...startChartJs1!,
            };
        }
    }, []);
    return chartUpdater;
};

export const Demo = () => {
    const [form, setForm] = useState<MatrixDto[]>(initialState);
    const threeJsController = useRef<{
        update: (form: MatrixDto[]) => void;
        tearDown: () => void;
    } | null>(null);
    const chartUpdater = useCharts();

    function handleFormChange(form: MatrixDto[]) {
        setForm(form);
        threeJsController.current!.update(form);
    }

    // a useeffect calling threeJsController.tearDown on unmount
    useEffect(() => {
        return () => {
            threeJsController.current?.tearDown();
        };
    }, []);

    useEffect(() => {
        if (!threeJsInitialized) {
            const result = startThree(form, circleData, weightsAndBiasesDimensions);
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
                        handleFormChange={(newMatrix: MatrixDto) => {
                            const newMatrices = [...form];
                            newMatrices[index] = newMatrix;
                            handleFormChange(newMatrices);
                        }}
                    />
                );
            })}
            <button
                onClick={async () => {
                    const result: TrainedNeuralNetworkDto = await api.train({
                        trainingData: mlTrainingData,
                        hiddenLayerDimensions,
                    });
                    const formData: MatrixDto[] = castToSimpleNetworkLayer(result);
                    const statistics: CostUpdate[] = castToStats(result);
                    handleFormChange(formData);
                    chartUpdater.current!.updateChart(
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

                <ChartContainer>
                    <canvas id="chartCanvas" />
                </ChartContainer>
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
