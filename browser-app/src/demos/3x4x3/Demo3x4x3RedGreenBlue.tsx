import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { startThree, threeJsInitialized } from "./ThreeCode.ts";
import { circlesDataSets, TrainingData } from "./data.ts";

const trainingData = circlesDataSets.nonLinear.circularRegion;
const hiddenLayerDimensions = [4];

export type Matrix = {
    rows: number;
    cols: number;
    data: number[][];
};

const initialState: Matrix[] = [
    {
        rows: 3,
        cols: 3,
        data: [
            [0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0],
        ],
    },
    {
        rows: 2,
        cols: 4,
        data: [
            [0.0, 0.0, 0.0, 0.0],
            [0.0, 0.0, 0.0, 0.0],
        ],
    },
];

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

function castToSimpleNetworkLayer(data: unknown): Matrix[] {
    console.log(data);
    // throw error if not an array:
    if (!Array.isArray(data)) {
        throw new Error("data is not an array");
    }
    // throw error if the array is empty:
    if (data.length < 1) {
        throw new Error("data is not of length 1");
    }
    // each element of the array ought to be an object with properties rows, cols, data:
    data.forEach((element) => {
        if (
            typeof element !== "object" ||
            !element.hasOwnProperty("rows") ||
            !element.hasOwnProperty("columns") ||
            !element.hasOwnProperty("data")
        ) {
            throw new Error("data element is not an object with properties");
        }
    });

    return data;
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
    handleFormChange: (form: Matrix) => void;
    value: Matrix;
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

export const Demo3x4x3RedGreenBlue = () => {
    const [form, setForm] = useState<Matrix[]>(initialState);
    const controls = useRef<{
        hasChanged: boolean;
        formValues: Matrix[];
    } | null>(null);

    function handleFormChange(form: Matrix[]) {
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
                    const result = await train(trainingData, hiddenLayerDimensions);
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
