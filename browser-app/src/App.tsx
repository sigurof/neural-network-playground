import { useEffect } from "react";
import { myDemo } from "./my-demo";
import * as THREE from "three";
import circlesVtxSource from "./three/circles/vertex.shader?raw";
import circlesFragSource from "./three/circles/fragment.shader?raw";

async function initiateCanvas() {
    await myDemo();
}

let threeJsInitialized = false;

function startThree() {
    let elementById: HTMLCanvasElement | null = document.getElementById(
        "canvas",
    ) as HTMLCanvasElement | null;
    if (!elementById) {
        console.warn("No canvas element found");
        return;
    }
    const scene = new THREE.Scene();
    // const frustumSize = 5;
    const aspect = window.innerWidth / window.innerHeight;
    const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0.1, 100);

    const renderer = new THREE.WebGLRenderer({
        canvas: elementById,
        antialias: true,
    });
    renderer.setSize(window.innerWidth, window.innerHeight);
    const geometry = new THREE.PlaneGeometry(1, 1);
    const material = new THREE.RawShaderMaterial({
        vertexShader: circlesVtxSource,
        fragmentShader: circlesFragSource,
        uniforms: {
            aspect: { value: aspect },
            radius: { value: 1 },
            center: { value: new THREE.Vector2(0.0, 0) },
            color: { value: new THREE.Color(1, 1, 0) },
        },
    });
    const circle = new THREE.Mesh(geometry, material);
    scene.add(circle);

    // camera.position.z = 1;
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
