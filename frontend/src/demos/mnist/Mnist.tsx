import styled from "styled-components";
import { MutableRefObject, useCallback, useEffect, useRef, useState } from "react";
import LinePlot from "./LinePlot.tsx";
import { SectionBox } from "./Common.tsx";
import { CreateAndTrainModel } from "./create-model/CreateAndTrainModel.tsx";
import { NeuralNetwork } from "../../common/ml/neural-network.ts";
import { InputVsOutput, Update } from "../../api/api.ts";

const DemoBed = styled.div`
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    grid-gap: 1rem;
`;

const CostGraph = ({ cost }: { cost: number[] }) => {
    console.log("cost", cost);
    return (
        <SectionBox>
            <h2>Network Performance</h2>
            <div>
                <LinePlot data={cost} />
            </div>
        </SectionBox>
    );
};

const TestingGrounds = () => {
    return (
        <SectionBox>
            <h2>Testing Grounds</h2>
        </SectionBox>
    );
};

// new NeuralNetwork().evaluateCost([])

type OnNeuralNetworkUpdate = (update: Update) => void;
export const Mnist = () => {
    // const [cost, setCost] = useState<number[]>([]);
    const [lines, setLines] = useState<Record<string, number[]>>({
        cost: [],
    });
    const [testData, setTestData] = useState<InputVsOutput[]>([]);
    const handleUpdate = (name: string, newValue: number) => {
        setLines({
            ...lines,
            [name]: [...lines[name], newValue],
        });
    };
    const onCostUpdate: OnNeuralNetworkUpdate = useCallback(
        (update: Update) => {
            const cost = testData.length > 0 ? new NeuralNetwork(update.neuralNetwork).evaluateCost(testData) : 0;
            handleUpdate("cost", cost);
        },
        [lines],
    );
    const onCostUpdateRef: MutableRefObject<OnNeuralNetworkUpdate> = useRef(onCostUpdate);

    useEffect(() => {
        onCostUpdateRef.current = onCostUpdate;
    }, [onCostUpdate]);
    return (
        <DemoBed>
            <CreateAndTrainModel
                onTestDataLoaded={(inputsVsOutputs) => {
                    console.log(`Loaded test data with length ${inputsVsOutputs.length}`);
                    setTestData(inputsVsOutputs);
                }}
                onNeuralNetworkUpdate={onCostUpdateRef}
            />
            <CostGraph cost={lines.cost} />
            <TestingGrounds />
            <div></div>
        </DemoBed>
    );
};
