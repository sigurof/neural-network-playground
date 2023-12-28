import { ChangeEvent, useEffect, useRef, useState } from "react";
import { myDemo } from "./my-demo";
import * as THREE from "three";
import circlesVtxSource from "./three/circles/vertex.shader?raw";
import circlesFragSource from "./three/circles/fragment.shader?raw";
import networkVtxSource from "./three/network/vertex.shader?raw";
import networkFragSource from "./three/network/fragment.shader?raw";

async function initiateCanvas() {
    await myDemo();
}

let threeJsInitialized = false;

const SQUARE: number[] = [
    -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0,
];

function createBillboardMesh(aspect: number) {
    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: circlesVtxSource,
        fragmentShader: circlesFragSource,
        uniforms: {
            aspect: { value: aspect },
            radius: { value: 0.01 },
            center: { value: new THREE.Vector2(0.0, 0) },
            color: { value: new THREE.Color(1, 1, 0) },
        },
    });
    const geometry = new THREE.InstancedBufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );
    let numCircles = 1000;
    const circlePositions = Array.from({ length: numCircles }, () => ({
        x: (Math.random() - 0.5) * 2,
        y: (Math.random() - 0.5) * 2,
    }));

    const circleTranslationsArray = new Float32Array(numCircles * 2);
    circlePositions.forEach((position, index) => {
        let positionIndex = index * 2;
        circleTranslationsArray[positionIndex] = position.x;
        circleTranslationsArray[positionIndex + 1] = position.y;
    });
    const BLUE = new THREE.Color(0, 0, 1);
    const RED = new THREE.Color(1, 0, 0);
    const circleColorsArray = new Float32Array(numCircles * 3);
    circlePositions.forEach(({ x, y }, index) => {
        const color = x + y < 0.5 ? BLUE : RED;
        let positionIndex = index * 3;
        circleColorsArray[positionIndex] = color.r;
        circleColorsArray[positionIndex + 1] = color.g;
        circleColorsArray[positionIndex + 2] = color.b;
    });
    geometry.setAttribute(
        "translate",
        new THREE.InstancedBufferAttribute(circleTranslationsArray, 2),
    );
    geometry.setAttribute(
        "colorInstanced",
        new THREE.InstancedBufferAttribute(circleColorsArray, 3),
    );
    return new THREE.Mesh(geometry, material);
}

function createBackgroundMesh(
    aspect: number,
    startValues: {
        weight00: number;
        weight01: number;
        weight10: number;
        weight11: number;
        bias0: number;
        bias1: number;
    },
) {
    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );
    let neuralNetworkWeights = new THREE.Matrix3(
        startValues.weight00,
        startValues.weight01,
        startValues.bias0,
        startValues.weight10,
        startValues.weight11,
        startValues.bias1,
        0,
        0,
        0,
    );
    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: networkVtxSource,
        fragmentShader: networkFragSource,
        depthTest: true,
        uniforms: {
            aspect: { value: aspect },
            fragMatrix: {
                value: neuralNetworkWeights,
            },
        },
    });
    const circleTranslationsArray = new Float32Array(2);
    circleTranslationsArray[0] = 0.0;
    circleTranslationsArray[1] = 0.0;
    geometry.setAttribute(
        "translate",
        new THREE.InstancedBufferAttribute(circleTranslationsArray, 2),
    );
    return {
        neuralNetworkWeights,
        mesh: new THREE.Mesh(geometry, material),
    };
}

function createScene() {
    let elementById: HTMLCanvasElement | null = document.getElementById(
        "canvas",
    ) as HTMLCanvasElement | null;
    if (!elementById) {
        console.warn("No canvas element found");
        return;
    }
    const scene = new THREE.Scene();
    const aspect = window.innerWidth / window.innerHeight;
    const camera = new THREE.OrthographicCamera(0, 1, 1, -1, 0.1, 100);
    const renderer = new THREE.WebGLRenderer({
        canvas: elementById,
        antialias: true,
    });
    renderer.setSize(window.innerWidth, window.innerHeight);
    // two triangles of a square
    return { scene, renderer, aspect, camera };
}

let now,
    delta,
    then = Date.now();
const interval = 1000 / 30;

function startThree(startValues: {
    weight00: number;
    weight01: number;
    weight10: number;
    weight11: number;
    bias0: number;
    bias1: number;
}) {
    const sceneInfo = createScene();
    if (!sceneInfo) {
        return;
    }

    const { scene, renderer, aspect, camera } = sceneInfo;
    let billboardMesh = createBillboardMesh(aspect);
    scene.add(billboardMesh);
    const { mesh, neuralNetworkWeights } = createBackgroundMesh(
        aspect,
        startValues,
    );
    scene.add(mesh);
    const controls = {
        // 4 weights
        hasChanged: false,
        formValues: startValues,
    };

    function animate() {
        requestAnimationFrame(animate);
        now = Date.now();
        delta = now - then;
        if (delta > interval) {
            // update fragMatrix depending on time
            const time = now * 0.001;

            if (controls.hasChanged) {
                controls.hasChanged = false;
                neuralNetworkWeights.set(
                    controls.formValues.weight00,
                    controls.formValues.weight01,
                    controls.formValues.bias0,
                    controls.formValues.weight10,
                    controls.formValues.weight11,
                    controls.formValues.bias1,
                    0,
                    0,
                    0,
                );
            }

            then = now - (delta % interval);
        }
        renderer.render(scene, camera);
    }

    animate();
    console.log("Three.js initialized");
    threeJsInitialized = true;
    return {
        controls,
    };
}

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
                min="-2" // Set the minimum value of the slider
                value={value}
                max="2" // Set the maximum value of the slider
                step="0.01" // Set the step size for each slide move
                onChange={handleChange}
            />
        </div>
    );
};

const GetInput = ({
    index,
    value,
    handleChange,
}: {
    index: number;
    value: number;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}) => {
    return (
        <input
            id={"weight" + index}
            type="range"
            min="-2" // Set the minimum value of the slider
            value={value}
            max="2" // Set the maximum value of the slider
            step="0.01" // Set the step size for each slide move
            onChange={handleChange}
        />
    );
};

function GetBias({
    index,
    value,
    handleChange,
}: {
    index: number;
    value: number;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}) {
    return (
        <input
            id={"bias" + index}
            type="range"
            min="-2" // Set the minimum value of the slider
            value={value}
            max="2" // Set the maximum value of the slider
            step="0.01" // Set the step size for each slide move
            onChange={handleChange}
        />
    );
}

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

function App() {
    // const [weight1, setWeight1] = useState(0.5);
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
            const result = startThree(startValues);
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
            <canvas id="canvas"></canvas>
        </>
    );
}

export default App;
