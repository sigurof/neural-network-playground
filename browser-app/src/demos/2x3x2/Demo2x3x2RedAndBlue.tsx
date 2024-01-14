import React, { ChangeEvent, useEffect, useRef, useState } from "react";
import axios from "axios";
import { startThree, threeJsInitialized } from "./ThreeCode.ts";
import { Input, WeightOrBiasName } from "../2x2/Demo2x2RedBlue.tsx";

export type Form = {
    bias0: number;
    weight00: number;
    weight11: number;
    weight01: number;
    weight10: number;
    bias1: number;
};

const initialState: Form = {
    weight00: Math.random(),
    weight01: Math.random(),
    weight10: Math.random(),
    weight11: Math.random(),
    bias0: Math.random(),
    bias1: Math.random(),
};

const numberOfCircles = 100;
type CircleData = {
    position: { x: number; y: number };
    color: "red" | "blue";
};
const circlesDataSet: CircleData[] = [...Array(numberOfCircles)].map((_) => {
    const x = (Math.random() - 0.5) * 2;
    const y = (Math.random() - 0.5) * 2;
    return {
        position: {
            x: x,
            y: y,
        },
        color: x + y < 0.5 ? "red" : "blue",
    };
});

const BLUE_ARRAY = [0.0, 1.0];
const RED_ARRAY = [1.0, 0.0];
type TrainingPoint = { input: number[]; output: number[] };
type TrainingData = TrainingPoint[];
const circlesTrainingData: TrainingData = circlesDataSet.map((circleData) => ({
    input: [circleData.position.x, circleData.position.y],
    output: circleData.color === "red" ? RED_ARRAY : BLUE_ARRAY,
}));

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

function castToSimpleNetworkLayer(data: unknown): Form {
    // throw error if not an array:
    if (!Array.isArray(data)) {
        throw new Error("data is not an array");
    }
    // throw error if the array is not of length 1:
    if (data.length !== 1) {
        throw new Error("data is not of length 1");
    }
    // throw error if the array's contained type doesn't have a data property:
    if (!("data" in data[0])) {
        throw new Error("data[0] does not have a data property");
    }
    return {
        weight00: data[0].data[0],
        weight01: data[0].data[1],
        bias0: data[0].data[2],
        weight10: data[0].data[3],
        weight11: data[0].data[4],
        bias1: data[0].data[5],
    };
}

export const Demo2x3x2RedAndBlue = () => {
    const [startValues, setStartValues] = useState<Form>(initialState);
    const controls = useRef<{
        hasChanged: boolean;
        formValues: Form;
    } | null>(null);

    function handleFormChange(form: Form) {
        setStartValues(form);
        controls.current!.formValues = form;
        controls.current!.hasChanged = true;
    }

    function handleChange(
        e: ChangeEvent<HTMLInputElement>,
        name: WeightOrBiasName,
    ) {
        const value = Number(e.target.value);
        if (!isNaN(value)) {
            const newForm = {
                ...startValues,
                [name]: value,
            };
            handleFormChange(newForm);
        }
    }

    useEffect(() => {
        if (!threeJsInitialized) {
            const result = startThree(startValues, circlesTrainingData);
            if (result) {
                controls.current = result.controls;
            }
        }
    }, []);

    return (
        <>
            {Object.keys(startValues).map((name) => (
                <Input
                    key={`input${name}`}
                    name={name}
                    value={startValues[name as WeightOrBiasName]}
                    handleChange={(e) =>
                        handleChange(e, name as WeightOrBiasName)
                    }
                />
            ))}
            <button
                onClick={async () => {
                    const result = await train(circlesTrainingData, []);
                    const formData = castToSimpleNetworkLayer(result);
                    console.log(formData);
                    handleFormChange(formData);
                }}
            >
                Train!
            </button>
            <canvas id="canvas"></canvas>
        </>
    );
};
