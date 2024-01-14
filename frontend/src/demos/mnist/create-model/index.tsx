import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { Box, Button, CircularProgress, Slider, TextField, Typography } from "@mui/material";
import { SectionBox } from "../Common.tsx";
import styled from "styled-components";
import { styled as muiStyled } from "@mui/material/styles";
import { OverrideDialog } from "./OverrideDialog.tsx";

function valuetext(value: number) {
    return `${value}°C`;
}

const gridContainer = {
    width: "400px",
    maxHeight: "200px",
    display: "grid",
    gridTemplateColumns: "1fr",
    gridTemplateRows: "1fr 1fr",
};

const Grid = styled.div`
    margin: 8px;
    display: grid;
    grid-template-columns: 1fr 2fr;
    align-items: center;
`;

const ButtonContainer = styled.div`
    display: flex;
    justify-content: right;
`;

// @ts-expect-error asdf
const LargeButton = muiStyled(Button)(({ theme }: { theme: any }) => ({
    margin: "0.5rem",
    width: "200px",
    height: "100px",
    fontSize: "1.2rem",
}));

function continueEvent(sessionId: string): string {
    return JSON.stringify({
        type: "Continue",
        sessionId,
    });
}

function newModelEvent(sessionId: string, layers: string, numTraining: number, override: boolean) {
    const payload = {
        type: "NewModel",
        hiddenLayers: layers
            .split(",")
            .map((it) => it.trim())
            .filter((it) => it)
            .map((it) => parseInt(it)),
        sizeDataSet: numTraining,
        sessionId,
        override,
    };
    return JSON.stringify(payload);
}

type SessionDto = {
    id: string;
    awaitingUserResponse: boolean;
    progress: number;
    result: string;
    isActive: boolean;
    model?: never;
};

export const CreateModel = () => {
    const [layers, setLayers] = useState<string>("");
    const [numTraining, setNumTraining] = useState<number>(50000);
    const [running, setRunning] = useState(false);
    const [askToOverride, setAskToOverride] = useState(false);
    const webSocket = useRef<WebSocket | null>();
    const [sessions, setSessions] = useState<SessionDto[]>([]);
    const [awaitingResponse, setAwaitingResponse] = useState(false);
    useEffect(() => {
        axios.get("http://localhost:8080/ml/sessions").then((res) => {
            setSessions(res.data);
        });
    }, []);
    useEffect(() => {
        const webs = webSocket.current;
        return () => {
            webs?.close();
        };
    }, []);

    const closeModal = () => setAskToOverride(false);

    const closeWebsocket = () => webSocket.current?.close();
    return (
        <SectionBox
            style={{
                display: "grid",
                gridTemplateColumns: "1fr",
                gridTemplateRows: "10% 1fr 30%",
            }}
        >
            <h2>Settings</h2>
            <OverrideDialog
                open={askToOverride}
                onCancel={() => {
                    closeModal();
                    closeWebsocket();
                    setRunning(false);
                }}
                onContinue={() => {
                    closeModal();
                    webSocket.current?.send(continueEvent("asdf"));
                }}
                onOverride={() => {
                    closeModal();
                    webSocket.current?.send(newModelEvent("asdf", layers, numTraining, true));
                }}
            />
            <Box sx={gridContainer}>
                <Grid>
                    <TextField
                        style={{
                            gridColumnStart: 1,
                            gridColumnEnd: 3,
                        }}
                        value={layers}
                        onChange={(e) => {
                            setLayers(e.target.value);
                        }}
                        fullWidth
                        id="standard-basic"
                        label="Hidden Layer Dimensions"
                    />
                </Grid>
                <Grid>
                    <Typography id="continuous-slider" gutterBottom>
                        Training Data
                    </Typography>
                    <Slider
                        max={60000}
                        min={1000}
                        step={1000}
                        value={numTraining}
                        onChange={(_, v) => {
                            setNumTraining(v as number);
                        }}
                        aria-label="Always visible"
                        getAriaValueText={valuetext}
                        valueLabelDisplay="auto"
                    />
                </Grid>
            </Box>
            <ButtonContainer>
                <LargeButton
                    onClick={() => {
                        if (running) {
                            webSocket.current?.close();
                            setRunning(false);
                        } else {
                            setAwaitingResponse(true);
                            const socket = new WebSocket("ws://localhost:8080/ml/network");
                            socket.addEventListener("open", () => {
                                socket.send(newModelEvent("asdf", layers, numTraining, false));
                            });
                            socket.addEventListener("message", (event) => {
                                const data = JSON.parse(event.data);
                                if (data.type === "AskSetModel") {
                                    console.log("Are you fine with overriding the data?");
                                    setAskToOverride(true);
                                    setAwaitingResponse(false);
                                }
                                if (data.type === "Update") {
                                    console.log("Got back data! It's running!");
                                    setAwaitingResponse(false);
                                    setRunning(true);
                                }
                            });
                            webSocket.current = socket;
                        }
                    }}
                    color={running ? "error" : "success"}
                    variant="contained"
                >
                    {awaitingResponse && <CircularProgress size={24} />}
                    {running ? "Stop" : "Start"}
                </LargeButton>
            </ButtonContainer>
        </SectionBox>
    );
};
