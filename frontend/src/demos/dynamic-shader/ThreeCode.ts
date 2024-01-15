import * as THREE from "three";
import circlesVtxSource from "./shaders/circles/vertex.shader?raw";
import circlesFragSource from "./shaders/circles/fragment.shader?raw";
import networkVtxSource from "./shaders/network/vertex.shader?raw";
import networkFragSource from "./shaders/network/fragment.shader?raw";
import { NetworkConnection } from "./Demo.tsx";
import { MatrixDto } from "../../api/api.ts";

const SQUARE: number[] = [-1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0];

export type Color = { r: number; g: number; b: number };
export type Vec2D = { x: number; y: number };
export type CircleData = { pos: Vec2D; color: Color };

function createBillboardMesh(aspect: number, circlesInputOutput: CircleData[]) {
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

    const circleTranslationsArray = new Float32Array(
        circlesInputOutput.length * 2,
    );
    circlesInputOutput.forEach(({ pos }, index) => {
        const positionIndex = index * 2;
        circleTranslationsArray[positionIndex] = pos.x;
        circleTranslationsArray[positionIndex + 1] = pos.y;
    });
    const circleColorsArray = new Float32Array(circlesInputOutput.length * 3);
    circlesInputOutput.forEach(({ color }, index) => {
        const positionIndex = index * 3;
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
    startValues: MatrixDto[],
    weightsAndBiasesDimensions: NetworkConnection[],
) {
    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );

    const { allWeightsAndBiases } = createLayers(startValues);

    const numberOfMatrices = weightsAndBiasesDimensions.length;
    const matrixDimensions = weightsAndBiasesDimensions.flatMap((it) => [
        it.rows,
        it.columns,
    ]);
    console.log(`All weights and biases: ${allWeightsAndBiases.length}`)
    console.log(`Matrix dimensions: ${matrixDimensions.length}`)

    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: networkVtxSource,
        fragmentShader: networkFragSource,
        depthTest: true,
        uniforms: {
            aspect: { value: aspect },
            allWeightsAndBiases: { value: allWeightsAndBiases },
            matrixDimensions: { value: new Int32Array(matrixDimensions) },
            numberOfMatrices: { value: numberOfMatrices },
        },
    });
    const circleTranslationsArray = new Float32Array(2);
    circleTranslationsArray[0] = 0.0;
    circleTranslationsArray[1] = 0.0;
    geometry.setAttribute(
        "translate",
        new THREE.InstancedBufferAttribute(circleTranslationsArray, 2),
    );
    return new THREE.Mesh(geometry, material);
}

function createScene() {
    const elementById: HTMLCanvasElement | null = document.getElementById(
        "threeCanvas",
    ) as HTMLCanvasElement | null;
    console.log(elementById);
    if (!elementById) {
        console.warn("No canvas element found");
        return;
    }
    const scene = new THREE.Scene();
    const width = 400;
    const height = 400;
    const aspect = width / height;
    const camera = new THREE.OrthographicCamera(0, 1, 1, -1, 0.1, 100);
    const renderer = new THREE.WebGLRenderer({
        canvas: elementById,
        antialias: true,
    });
    renderer.setSize(width, height);
    return { scene, renderer, aspect, camera };
}

let now,
    delta,
    then = Date.now();
let fps = 3;
const interval = 1000 / fps;

function createLayers(networkLayers: MatrixDto[]): {
    allWeightsAndBiases: Float32Array;
} {
    const numbers: number[] = networkLayers.flatMap((matrix: MatrixDto) =>
        matrix.data.flatMap((row) => row),
    );
    return {
        allWeightsAndBiases: new Float32Array([...numbers]),
    };
}

export let threeJsInitialized = false;

export function startThree(
    startValues: MatrixDto[],
    circlesInputOutput: CircleData[],
    weightsAndBiasesDimensions: NetworkConnection[],
) {
    const sceneInfo = createScene();
    if (!sceneInfo) {
        return;
    }

    const { scene, renderer, aspect, camera } = sceneInfo;
    const billboardMesh = createBillboardMesh(aspect, circlesInputOutput);
    scene.add(billboardMesh);
    const backgroundMesh = createBackgroundMesh(
        aspect,
        startValues,
        weightsAndBiasesDimensions,
    );
    scene.add(backgroundMesh);
    const controls = {
        hasChanged: false,
        formValues: startValues,
    };

    function animate() {
        requestAnimationFrame(animate);
        now = Date.now();
        delta = now - then;
        if (delta > interval) {
            const time = now * 0.001;

            if (controls.hasChanged) {
                console.log("Updating weights and biases");
                controls.hasChanged = false;
                const { allWeightsAndBiases } = createLayers(
                    controls.formValues,
                );
                backgroundMesh.material.uniforms.allWeightsAndBiases.value =
                    allWeightsAndBiases;
            }

            then = now - (delta % interval);
        }
        renderer.render(scene, camera);
    }

    animate();
    threeJsInitialized = true;

    const threeJsUUID: string =   Math.random().toString(36).substring(7);
    console.log(`Three.js ${threeJsUUID} initialized`);

    const update = (form: MatrixDto[]) => {
        controls.formValues = form;
        controls.hasChanged = true;
    };
    const tearDown = ()=>{
        console.log(`Three.js ${threeJsUUID} tearing down`);
        threeJsInitialized = false;
        billboardMesh.geometry.dispose();
        billboardMesh.material.dispose();
        backgroundMesh.geometry.dispose();
        backgroundMesh.material.dispose();
        scene.remove(billboardMesh);
        scene.remove(backgroundMesh);
    }

    return {
        update,
        tearDown,
    };
}
