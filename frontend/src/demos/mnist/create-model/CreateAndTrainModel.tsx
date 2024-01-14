import { RefObject, useEffect, useRef, useState } from "react";
import axios from "axios";
import { Button, CircularProgress, MenuItem, Select, Slider, TextField, Typography } from "@mui/material";
import { SectionBox } from "../Common.tsx";
import styled from "styled-components";
import { styled as muiStyled } from "@mui/material/styles";
import { OverrideDialog } from "./OverrideDialog.tsx";
import { toast } from "react-toastify";
import { NeuralNetwork } from "../../../common/ml/neural-network.ts";
import { api, InputVsOutput, ServerEvent, serverEvents, SessionDto, Update } from "../../../api/api.ts";

function valuetext(value: number) {
    return `${value}°C`;
}

const GridContainer = styled.div`
    width: 400px;
    max-height: 200px;
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 1fr 1fr;
`;

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

const clientEvents = {
    continue: `no.sigurof.ml.server.web.websockets.ClientEvent.Continue`,
    newModel: `no.sigurof.ml.server.web.websockets.ClientEvent.NewModel`,
};

type NewModel = {
    type?: string;
    sessionId: string;
    override: boolean;
    model: {
        hiddenLayers: number[];
        sizeDataSet: number;
        sizeTestSet: number;
    };
};

const createEvent = {
    continue(sessionId: string): string {
        return JSON.stringify({
            type: clientEvents.continue,
            sessionId,
        });
    },
    newModel(data: NewModel) {
        const payload: NewModel = {
            type: clientEvents.newModel,
            ...data,
        };
        return JSON.stringify(payload);
    },
};

function parseHiddenLayers(layers: string) {
    console.log(layers);
    return layers
        .split(",")
        .map((it) => it.trim())
        .filter((it) => it)
        .map((it) => parseInt(it));
}

export type OnTestDataLoaded = (testData: InputVsOutput[]) => void;
export const CreateAndTrainModel = ({
    onNeuralNetworkUpdate,
    onTestDataLoaded,
}: {
    onTestDataLoaded: OnTestDataLoaded;
    onNeuralNetworkUpdate: RefObject<(update: Update) => void>;
}) => {
    const [layers, setLayers] = useState<string>("");
    const [numTest, setNumTest] = useState<number>(1000);
    const [numTraining, setNumTraining] = useState<number>(50000);
    const [running, setRunning] = useState(false);
    const [askToOverride, setAskToOverride] = useState(false);
    const webSocket = useRef<WebSocket | null>();
    const [sessionIdSelect, setSessionIdSelect] = useState<string>("New");
    const [sessionId, setSessionId] = useState<string>("");
    const [sessions, setSessions] = useState<SessionDto[]>([]);
    const [awaitingResponse, setAwaitingResponse] = useState(false);
    useEffect(() => {
        api.getSessions().then((res) => setSessions(res));
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
                    webSocket.current?.send(createEvent.continue(sessionId));
                }}
                onOverride={() => {
                    closeModal();
                    const hiddenLayers = parseHiddenLayers(layers);
                    console.log(hiddenLayers);
                    webSocket.current?.send(
                        createEvent.newModel({
                            sessionId,
                            model: {
                                hiddenLayers,
                                sizeDataSet: numTraining,
                                sizeTestSet: numTest,
                            },
                            override: true,
                        }),
                    );
                }}
            />

            <GridContainer>
                <Grid>
                    <Select
                        value={sessionIdSelect}
                        onChange={(event, _) => {
                            const value = event.target.value as string;
                            setSessionIdSelect(value);
                            if (value !== "New") {
                                setSessionId(value);
                            } else {
                                setSessionId("");
                            }
                        }}
                    >
                        <MenuItem value={"New"}>New</MenuItem>
                        {sessions.map((it) => (
                            <MenuItem key={`session-${it.id}`} value={it.id}>
                                {it.id}
                            </MenuItem>
                        ))}
                    </Select>
                    {sessionIdSelect === "New" && (
                        <TextField
                            onChange={(e) => {
                                setSessionId(e.target.value);
                            }}
                            value={sessionId}
                        />
                    )}
                </Grid>
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
                <Grid>
                    <Typography id="continuous-slider" gutterBottom>
                        Test data
                    </Typography>
                    <Slider
                        max={10000}
                        min={100}
                        step={100}
                        value={numTest}
                        onChange={(_, v) => {
                            setNumTest(v as number);
                        }}
                        aria-label="Always visible"
                        getAriaValueText={valuetext}
                        valueLabelDisplay="auto"
                    />
                </Grid>
            </GridContainer>
            <ButtonContainer>
                <LargeButton
                    onClick={() => {
                        api.getTestData(numTest).then((testData) => onTestDataLoaded(testData));
                        if (running) {
                            webSocket.current?.close();
                            setRunning(false);
                        } else {
                            setAwaitingResponse(true);
                            const socket = new WebSocket("ws://localhost:8080/ml/network");
                            socket.addEventListener("open", () => {
                                console.log("opened");
                                socket.send(
                                    createEvent.newModel({
                                        sessionId,
                                        model: {
                                            hiddenLayers: parseHiddenLayers(layers),
                                            sizeDataSet: numTraining,
                                            sizeTestSet: numTest,
                                        },
                                        override: false,
                                    }),
                                );
                            });
                            socket.addEventListener("message", (event) => {
                                const data: ServerEvent = JSON.parse(event.data);
                                if (data.type === serverEvents.askSetModel) {
                                    console.log("Are you fine with overriding the data?");
                                    setAskToOverride(true);
                                    setAwaitingResponse(false);
                                }
                                if (data.type === serverEvents.update) {
                                    setAwaitingResponse(false);
                                    setRunning(true);
                                    onNeuralNetworkUpdate.current?.(data);
                                }
                                if (data.type === serverEvents.clientError) {
                                    console.log("Client error: ", data.message);
                                    toast.error(data.message);
                                    setAwaitingResponse(false);
                                    setRunning(false);
                                    webSocket.current?.close();
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
