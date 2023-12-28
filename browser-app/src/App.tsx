import { useEffect } from "react";
import { myDemo } from "./my-demo";
import * as THREE from "three";
import circlesVtxSource from "./three/circles/vertex.shader?raw";
import circlesFragSource from "./three/circles/fragment.shader?raw";
import networkVtxSource from "./three/network/vertex.shader?raw";
import networkFragSource from "./three/network/fragment.shader?raw";
import { InstancedBufferGeometry } from "three";

async function initiateCanvas() {
    await myDemo();
}

let threeJsInitialized = false;

const SQUARE: number[] = [
    -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0,
];

function uploadCircleData(geometry: InstancedBufferGeometry ) {
    let numCircles = 5;
    let circlePositions = [...Array(numCircles)].map((_) => {
        return { x: Math.random(), y: Math.random() };
    });
    const circleTranslationsArray = new Float32Array(numCircles * 2);
    circlePositions.forEach((position, index) => {
        let positionIndex = index * 2;
        circleTranslationsArray[positionIndex] = position.x;
        circleTranslationsArray[positionIndex + 1] = position.y;
    });
    geometry.setAttribute(
        "translate",
        new THREE.InstancedBufferAttribute(circleTranslationsArray, 2),
    );
}

function createBillboardMesh(aspect: number) {
    const geometry = new THREE.InstancedBufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );
    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: circlesVtxSource,
        fragmentShader: circlesFragSource,
        uniforms: {
            aspect: { value: aspect },
            radius: { value: 0.1 },
            center: { value: new THREE.Vector2(0.0, 0) },
            color: { value: new THREE.Color(1, 1, 0) },
        },
    });
    uploadCircleData(geometry);
    return new THREE.Mesh(geometry, material);
}

function createBackgroundMesh(aspect: number) {
    const geometry = new THREE.InstancedBufferGeometry();
    geometry.setAttribute(
        "position",
        new THREE.Float32BufferAttribute(SQUARE, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );
    const material = new THREE.RawShaderMaterial({
        glslVersion: THREE.GLSL3,
        vertexShader: networkVtxSource,
        fragmentShader: networkFragSource,
        uniforms: {
            aspect: { value: aspect },
        },
    });
    return new THREE.Mesh(geometry, material);
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
    const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0.1, 100);
    const renderer = new THREE.WebGLRenderer({
        canvas: elementById,
        antialias: true,
    });
    renderer.setSize(window.innerWidth, window.innerHeight);
    // two triangles of a square
    return { scene, renderer, aspect, camera };
}

function startThree() {
    const sceneInfo = createScene();
    if (!sceneInfo) {
        return;
    }

    const { scene, renderer, aspect, camera } = sceneInfo;
    scene.add(createBillboardMesh(aspect));
    scene.add(createBackgroundMesh(aspect));

    function animate() {
        requestAnimationFrame(animate);
        renderer.render(scene, camera);
    }

    animate();
    threeJsInitialized = true;
}

function App() {
    useEffect(() => {
        if (!threeJsInitialized) {
            startThree();
        }
    }, []);
    return (
        <>
            <div>Hello world</div>
            <canvas id="canvas"></canvas>
        </>
    );
}

export default App;
