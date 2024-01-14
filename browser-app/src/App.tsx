import { useEffect } from "react";
import { myDemo } from "./my-demo";

async function initiateCanvas() {
    await myDemo();
}

function App() {
    useEffect(() => {
        initiateCanvas();
    }, []);
    return (
        <>
            <div>Hello world</div>
            <canvas
                style={{ border: "1px solid black" }}
                id="canvas"
                width="800"
                height="600"
            ></canvas>
        </>
    );
}

export default App;
