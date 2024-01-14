import styled from "styled-components";
import { useState } from "react";
import LinePlot from "./LinePlot.tsx";
import { SectionBox } from "./Common.tsx";
import { CreateModel } from "./create-model";

const DemoBed = styled.div`
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    grid-gap: 1rem;
`;

const CostGraph = () => {
    const [data, setData] = useState<number[]>([1, 2, 3]);
    return (
        <SectionBox>
            <h2>Network Performance</h2>
            <div>
                <LinePlot data={data} />
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

export const Demo = () => {
    return (
        <DemoBed>
            <CreateModel />
            <CostGraph />
            <TestingGrounds />
            <div></div>
        </DemoBed>
    );
};
