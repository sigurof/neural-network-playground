import React, { ChangeEvent, useEffect, useRef, useState } from "react";
import axios from "axios";
import { startThree, tearDownScene, threeJsInitialized } from "./ThreeCode.ts";

export const Input = ({
    name,
    value,
    handleChange,
}: {
    name: string;
    value: number;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
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
                onChange={handleChange}
            />
        </div>
    );
};

export type Form = {
    bias0: number;
    weight00: number;
    weight11: number;
    weight01: number;
    weight10: number;
    bias1: number;
};

export type WeightOrBiasName = keyof Form;

const initialState: Form = {
    weight00: Math.random(),
    weight01: Math.random(),
    weight10: Math.random(),
    weight11: Math.random(),
    bias0: Math.random(),
    bias1: Math.random(),
};

const circlesInputOutput: { input: number[]; output: number[] }[] = Array.from(
    { length: 100 },
    () => {
        const x = (Math.random() - 0.5) * 2;
        const y = (Math.random() - 0.5) * 2;
        const blue = [0.0, 1.0];
        const red = [1.0, 0.0];
        return {
            input: [x, y],
            output: x + y < 0.5 ? red : blue,
        };
    },
);

async function train(): Promise<Form> {
    const res = await axios.post(
        "http://localhost:8080/ml/network",
        {
            trainingData: circlesInputOutput,
            hiddenLayerDimensions: [],
        },
        {
            headers: {
                "Content-Type": "application/json",
            },
        },
    );
    return {
        weight00: res.data[0].data[0][0],
        weight01: res.data[0].data[0][1],
        bias0: res.data[0].data[0][2],
        weight10: res.data[0].data[1][0],
        weight11: res.data[0].data[1][1],
        bias1: res.data[0].data[1][2],
    };
}

export const Demo2x2RedBlue = () => {
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
            const result = startThree(startValues, circlesInputOutput);
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
                onClick={() => train().then((form) => handleFormChange(form))}
            >
                Train!
            </button>
            <canvas id="canvas"></canvas>
        </>
    );
};
