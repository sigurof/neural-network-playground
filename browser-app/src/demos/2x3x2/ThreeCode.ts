import * as THREE from "three";
import circlesVtxSource from "../../three/circles/vertex.shader?raw";
import circlesFragSource from "../../three/circles/fragment.shader?raw";
import networkVtxSource from "../../three/network/vertex.shader?raw";
import networkFragSource from "../../three/network/fragment.shader?raw";

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
        circleColorsArray[positionIndex + 1] = 0; // green channel always zero
        circleColorsArray[positionIndex + 2] = output[1];
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
    const neuralNetworkWeights = new THREE.Matrix3(
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
    const elementById: HTMLCanvasElement | null = document.getElementById(
        "canvas",
    ) as HTMLCanvasElement | null;
    if (!elementById) {
        console.warn("No canvas element found");
        return;
    }
    const scene = new THREE.Scene();
    const width = 800;
    const height = 600;
    // const width = window.innerWidth;
    // const height = window.innerHeight;
    const aspect = width / height;
    const camera = new THREE.OrthographicCamera(0, 1, 1, -1, 0.1, 100);
    const renderer = new THREE.WebGLRenderer({
        canvas: elementById,
        antialias: true,
    });
    renderer.setSize(width, height);
    // two triangles of a square
    return { scene, renderer, aspect, camera };
}

let now,
    delta,
    then = Date.now();
const interval = 1000 / 30;

export function startThree(
    startValues: {
        weight00: number;
        weight01: number;
        weight10: number;
        weight11: number;
        bias0: number;
        bias1: number;
    },
    circlesInputOutput: { input: number[]; output: number[] }[],
) {
    const sceneInfo = createScene();
    if (!sceneInfo) {
        return;
    }

    const { scene, renderer, aspect, camera } = sceneInfo;
    const billboardMesh = createBillboardMesh(aspect, circlesInputOutput);
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
    threeJsInitialized = true;
    return {
        controls,
    };
}

export let threeJsInitialized = false;
