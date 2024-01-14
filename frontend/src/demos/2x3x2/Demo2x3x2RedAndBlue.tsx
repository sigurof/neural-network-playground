import React, { useEffect, useRef, useState } from "react";
import { startThree, threeJsInitialized } from "./ThreeCode.ts";
import { circlesDataSets } from "./data.ts";
import { api, MatrixDto, TrainedNeuralNetworkDto } from "../../api/api.ts";

const trainingData = circlesDataSets.nonLinear.circularRegion;

const hiddenLayerDimensions = [3];
const initialState: MatrixDto[] = [
    {
        rows: 3,
        columns: 3,
        data: [
            [0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0],
        ],
    },
    {
        rows: 2,
        columns: 4,
        data: [
            [0.0, 0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0, 0.0],
        ],
    },
];

function castToSimpleNetworkLayer(data: TrainedNeuralNetworkDto): MatrixDto[] {
    console.log(data);
    // type safely pick layers property from data:
    // const data2 = (data as { layers: unknown }).layers;
    const data2 = data.neuralNetwork.connections;
    // throw error if not an array:
    if (!Array.isArray(data2)) {
        throw new Error("data is not an array");
    }
    // throw error if the array is empty:
    if (data2.length < 1) {
        throw new Error("data is not of length 1");
    }
    // each element of the array ought to be an object with properties rows, cols, data:
    const matrixDtos = data2.map((it) => it.matrix);
    matrixDtos.forEach((element) => {
        if (
            typeof element !== "object" ||
            !Object.prototype.hasOwnProperty.call(element, "rows") ||
            !Object.prototype.hasOwnProperty.call(element, "columns") ||
            !Object.prototype.hasOwnProperty.call(element, "data")
        ) {
            throw new Error("data element is not an object with properties");
        }
    });

    return matrixDtos;
}

const WeightBiasInput = ({
    name,
    value,
    handleChange,
}: {
    name: string;
    value: number;
    handleChange: (value: string) => void;
}) => {
    return (
        <div>
            <label htmlFor={"input" + name}>{name}</label>
            <input
                id={"input" + name}
                type="range"
                min="-10" // Set the minimum value of the slider
                value={value}
                max="10" // Set the maximum value of the slider
                step="0.01" // Set the step size for each slide move
                onChange={(e) => handleChange(e.target.value)}
            />
        </div>
    );
};

const MatrixRowInput = ({
    name,
    row,
    handleChange,
}: {
    name: string;
    row: number[];
    handleChange: (value: number[]) => void;
}) => {
    console.log(row);
    return (
        <div>
            {row.map((value, index) => {
                return (
                    <WeightBiasInput
                        key={`input${name}${index}`}
                        name={`input${name}${index}`}
                        value={value}
                        handleChange={(newValue: string) => {
                            const valueAsNumber = Number(newValue);
                            if (isNaN(valueAsNumber)) {
                                return;
                            }
                            const newValues = [...row];
                            newValues[index] = valueAsNumber;
                            handleChange(newValues);
                        }}
                    />
                );
            })}
        </div>
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
    console.log(value);
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

export const Demo2x3x2RedAndBlue = () => {
    const [form, setForm] = useState<MatrixDto[]>(initialState);
    const controls = useRef<{
        hasChanged: boolean;
        formValues: MatrixDto[];
    } | null>(null);

    function handleFormChange(form: MatrixDto[]) {
        setForm(form);
        controls.current!.formValues = form;
        controls.current!.hasChanged = true;
    }

    useEffect(() => {
        if (!threeJsInitialized) {
            const result = startThree(form, trainingData);
            if (result) {
                controls.current = result.controls;
            }
        }
    }, []);

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
                        trainingData: trainingData,
                        hiddenLayerDimensions,
                    });
                    const formData = castToSimpleNetworkLayer(result);
                    handleFormChange(formData);
                }}
            >
                Train!
            </button>
            <canvas id="canvas"></canvas>
        </>
    );
};
