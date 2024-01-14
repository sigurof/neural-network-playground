import * as THREE from "three";
import circlesVtxSource from "../../three/circles/vertex.shader?raw";
import circlesFragSource from "../../three/circles/fragment.shader?raw";
import networkVtxSource from "./shaders/network/vertex.shader?raw";
import networkFragSource from "./shaders/network/fragment.shader?raw";
import { Matrix } from "./Demo2x4x3RedGreenBlue.tsx";

const SQUARE: number[] = [
    -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0,
];

function createBillboardMesh(
    aspect: number,
    circlesInputOutput: { input: number[]; output: number[] }[],
) {
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
    circlesInputOutput.forEach(({ input }, index) => {
        const positionIndex = index * 2;
        circleTranslationsArray[positionIndex] = input[0];
        circleTranslationsArray[positionIndex + 1] = input[1];
    });
    const circleColorsArray = new Float32Array(circlesInputOutput.length * 3);
    circlesInputOutput.forEach(({ output }, index) => {
        const positionIndex = index * 3;
        circleColorsArray[positionIndex] = output[0];
        circleColorsArray[positionIndex + 1] = output[1]; // green channel always zero
        circleColorsArray[positionIndex + 2] = output[2];
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

function createBackgroundMesh(aspect: number, startValues: Matrix[]) {
    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );

    const { firstWeights, secondWeights, thirdWeights } =
        createLayers(startValues);

    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: networkVtxSource,
        fragmentShader: networkFragSource,
        depthTest: true,
        uniforms: {
            aspect: { value: aspect },
            firstWeights: { value: firstWeights },
            secondWeights: { value: secondWeights },
            thirdWeights: { value: thirdWeights },
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
let fps = 30;
const interval = 1000 / fps;

function createLayers(networkLayers: Matrix[]): {
    firstWeights: THREE.Matrix3;
    secondWeights: THREE.Matrix4;
    thirdWeights: THREE.Matrix4;
} {
    const firstWeights = new THREE.Matrix3(
        networkLayers[0].data[0][0],
        networkLayers[0].data[0][1],
        networkLayers[0].data[0][2],
        networkLayers[0].data[1][0],
        networkLayers[0].data[1][1],
        networkLayers[0].data[1][2],
        networkLayers[0].data[2][0],
        networkLayers[0].data[2][1],
        networkLayers[0].data[2][2],
    );

    // only populating 2 first rows of the 4x4 matrix because the last ones are unused
    const secondWeights = new THREE.Matrix4(
        networkLayers[1].data[0][0],
        networkLayers[1].data[0][1],
        networkLayers[1].data[0][2],
        networkLayers[1].data[0][3],
        networkLayers[1].data[1][0],
        networkLayers[1].data[1][1],
        networkLayers[1].data[1][2],
        networkLayers[1].data[1][3],
        networkLayers[1].data[2][0],
        networkLayers[1].data[2][1],
        networkLayers[1].data[2][2],
        networkLayers[1].data[2][3],
        0,
        0,
        0,
        0,
    );

    const thirdWeights = new THREE.Matrix4(
        networkLayers[2].data[0][0],
        networkLayers[2].data[0][1],
        networkLayers[2].data[0][2],
        networkLayers[2].data[0][3],
        networkLayers[2].data[1][0],
        networkLayers[2].data[1][1],
        networkLayers[2].data[1][2],
        networkLayers[2].data[1][3],
        networkLayers[2].data[2][0],
        networkLayers[2].data[2][1],
        networkLayers[2].data[2][2],
        networkLayers[2].data[2][3],
        0,
        0,
        0,
        0,
    );

    return { firstWeights, secondWeights, thirdWeights };
}

export function startThree(
    startValues: Matrix[],
    circlesInputOutput: { input: number[]; output: number[] }[],
) {
    const sceneInfo = createScene();
    if (!sceneInfo) {
        return;
    }

    const { scene, renderer, aspect, camera } = sceneInfo;
    const billboardMesh = createBillboardMesh(aspect, circlesInputOutput);
    scene.add(billboardMesh);
    const backgroundMesh = createBackgroundMesh(aspect, startValues);
    scene.add(backgroundMesh);
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
                const { firstWeights, secondWeights, thirdWeights } =
                    createLayers(controls.formValues);
                // TODO Set the new values on the uniform here
                backgroundMesh.material.uniforms.firstWeights.value =
                    firstWeights;
                backgroundMesh.material.uniforms.secondWeights.value =
                    secondWeights;
                backgroundMesh.material.uniforms.thirdWeights.value =
                    thirdWeights;
            }

            then = now - (delta % interval);
        }
        renderer.render(scene, camera);
    }

    animate();
    threeJsInitialized = true;
    return {
        controls,
    };
}

export let threeJsInitialized = false;
