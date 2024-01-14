import styled from "styled-components";
import {
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Slider,
    TextField,
    Typography,
} from "@mui/material";
import { useEffect, useRef, useState } from "react";
import LinePlot from "./LinePlot.tsx";
import { styled as muiStyled } from "@mui/material/styles";
import axios from "axios";

// import Grid from "@mui/material/Unstable_Grid2";

const DemoBed = styled.div`
    min-height: 100vh;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    grid-gap: 1rem;
`;

const SliderContainer = styled.div`
    display: grid;
    grid-template-columns: 1fr 1fr;
`;

const MyLabel = styled.label`
    grid-column-start: 1;
`;

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

// const Grid = muiStyled(Box)(({ theme }: { theme: any }) => ({
//     margin: "8px",
//     display: "grid",
//     gridTemplateColumns: "1fr 2fr",
//     alignItems: "center",
// }));

const Grid = styled.div`
    margin: 8px;
    display: grid;
    grid-template-columns: 1fr 2fr;
    align-items: center;
`;

const SectionBox = styled.div`
    border: 2px solid #ededed;
    border-radius: 5px;
    padding: 1rem;
    background-color: #fafafa;
`;

// const Button = styled.button`
//     background-color: #3e54e6
//     color: white;
//     font-weight: bold;
//     shadow: 2px 2px 2px black;
// min-height: 2rem;
// box-shadow: 2px 2px 2px gray;
// border: 5px solid #3e54e6"
// border-radius: 0.5rem;
//
// `;

const ButtonContainer = styled.div`
    display: flex;
    justify-content: right;
    //grid-template-columns: 335;
    //grid-column-start: 1;
    //grid-gap: 1rem;
`;

// @ts-expect-error asdf
const LargeButton = muiStyled(Button)(({ theme }: { theme: any }) => ({
    margin: "0.5rem",
    width: "200px",
    height: "100px",
    fontSize: "1.2rem",
    // backgroundColor: "#24990c",
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

const useWebSocket = (url: string) => {
    const webSocket = useRef<WebSocket | null>();
    useEffect(() => {
        const webs = webSocket.current;
        return () => {
            webs?.close();
        };
    }, []);
    return {
        wsStart: () => {
            const ws = new WebSocket(url);
            webSocket.current = ws;
            return ws;
        },
        wsClose: webSocket.current?.close,
        wsSend: webSocket.current?.send,
    };
};

type SessionDto = {
    id: string;
    awaitingUserResponse: boolean;
    progress: number;
    result: string;
    isActive: boolean;
    model?: never;
};

const InputFields = () => {
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

    const onCancel = () => webSocket.current?.close();
    return (
        <SectionBox
            style={{
                display: "grid",
                gridTemplateColumns: "1fr",
                gridTemplateRows: "10% 1fr 30%",
            }}
        >
            <h2>Settings</h2>
            <Dialog open={askToOverride} onClose={closeModal}>
                <DialogTitle>Override?</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        A session with that ID already exists. Do you want to continue with the existing session or
                        override it?
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => {
                            closeModal();
                            onCancel();
                            setRunning(false);
                        }}
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={() => {
                            closeModal();
                            webSocket.current?.send(continueEvent("asdf"));
                            // setRunning(!running);
                        }}
                        autoFocus
                    >
                        Continue
                    </Button>
                    <Button
                        onClick={() => {
                            closeModal();
                            webSocket.current?.send(newModelEvent("asdf", layers, numTraining, true));
                            // setRunning(!running);
                        }}
                    >
                        Override
                    </Button>
                </DialogActions>
            </Dialog>
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
                        // marks={marks}
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
    // const { chartControls } = useCharts();
    return (
        <DemoBed>
            <InputFields />
            <CostGraph />
            <TestingGrounds />
            <div></div>
        </DemoBed>
    );
};
