import React, { ChangeEvent, useEffect, useRef, useState } from "react";
import axios, { AxiosResponse } from "axios";
import { startThree, threeJsInitialized } from "./ThreeCode.ts";

const GetInput2 = ({
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
                key={"input" + name}
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

type Form = {
    bias0: number;
    weight00: number;
    weight11: number;
    weight01: number;
    weight10: number;
    bias1: number;
};

type WeightOrBias = keyof Form;

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
        let blue = [0.0, 1.0];
        let red = [1.0, 0.0];
        return {
            input: [x, y],
            output: x + y < 0.5 ? red : blue,
        };
    },
);

export const Demo2x2RedBlue = () => {
    const [startValues, setStartValues] = useState<Form>(initialState);
    const controls = useRef<{
        hasChanged: boolean;
        formValues: Form;
    } | null>(null);

    function handleChange(
        e: ChangeEvent<HTMLInputElement>,
        name: WeightOrBias,
    ) {
        const value = Number(e.target.value);
        if (!isNaN(value)) {
            setStartValues((prev) => ({
                ...prev,
                [name]: value,
            }));
            controls.current!.hasChanged = true;
            controls.current!.formValues[name] = value;
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
    console.log(startValues);

    return (
        <>
            {Object.keys(startValues).map((it) => (
                <GetInput2
                    name={it}
                    value={startValues[it as WeightOrBias]}
                    handleChange={(e) => handleChange(e, it as WeightOrBias)}
                />
            ))}
            <button
                onClick={() => {
                    axios
                        .post(
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
                        )
                        .then(
                            (
                                res: AxiosResponse<
                                    {
                                        rows: number;
                                        cols: number;
                                        data: number[];
                                    }[]
                                >,
                            ) => {
                                console.log(res.data);
                                const newVar = {
                                    weight00: res.data[0].data[0],
                                    weight01: res.data[0].data[1],
                                    bias0: res.data[0].data[2],
                                    weight10: res.data[0].data[3],
                                    weight11: res.data[0].data[4],
                                    bias1: res.data[0].data[5],
                                };
                                setStartValues(newVar);
                                controls.current!.formValues = newVar;
                                controls.current!.hasChanged = true;
                            },
                        );
                }}
            >
                Train!
            </button>
            <canvas id="canvas"></canvas>
        </>
    );
};
