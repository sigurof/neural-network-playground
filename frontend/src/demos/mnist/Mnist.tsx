import styled from "styled-components";
import { MutableRefObject, useCallback, useEffect, useRef, useState } from "react";
import LinePlot from "./LinePlot.tsx";
import { SectionBox } from "./Common.tsx";
import { CreateModel } from "./create-model/CreateModel.tsx";

const DemoBed = styled.div`
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    grid-gap: 1rem;
`;

const CostGraph = ({ cost }: { cost: number[] }) => {
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

export type Matrix = {
    rows: number;
    columns: number;
    data: number[][];
};


export const Mnist = () => {
    const [cost, setCost] = useState<number[]>([]);
    const onCostUpdate = useCallback(
        (newCost: number) => {
            setCost([...cost, newCost]);
        },
        [cost],
    );
    const onCostUpdateRef: MutableRefObject<(cost: number) => void> = useRef(onCostUpdate);

    useEffect(() => {
        onCostUpdateRef.current = onCostUpdate;
    }, [onCostUpdate]);
    useEffect(() => {
        console.log("Rerendering Demo");
    }, []);
    return (
        <DemoBed>
            <CreateModel onCostUpdate={onCostUpdateRef} />
            <CostGraph cost={cost} />
            <TestingGrounds />
            <div></div>
        </DemoBed>
    );
};