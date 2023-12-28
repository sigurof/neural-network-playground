import { useEffect } from "react";
import { myDemo } from "./my-demo";
import * as THREE from "three";
import circlesVtxSource from "./three/circles/vertex.shader?raw";
import circlesFragSource from "./three/circles/fragment.shader?raw";
// import circlesVtxSource from "./my-demo/shaders/circles/vertex.shader?raw";
// import circlesFragSource from "./my-demo/shaders/circles/fragment.shader?raw";

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
    // Float array of 4 2d points
    const position = [
        // First triangle
        -1.0, -1.0,  // bottom left
        1.0, -1.0,  // bottom right
        1.0, 1.0, // top right

        // Second triangle
        -1.0, -1.0,  // bottom left
        1.0, 1.0,  // top right
        -1.0, 1.0, // top left
    ];
    // Upload positions to the geometry as static draw


    // const geometry = new THREE.PlaneGeometry(1, 1);
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
    // const circle = new THREE.Mesh(geometry, material);

    // const translateArray = new Float32Array( particleCount * 3 );
    //
    // for ( let i = 0, i3 = 0, l = particleCount; i < l; i ++, i3 += 3 ) {
    //
    //     translateArray[ i3 + 0 ] = Math.random() * 2 - 1;
    //     translateArray[ i3 + 1 ] = Math.random() * 2 - 1;
    //     translateArray[ i3 + 2 ] = Math.random() * 2 - 1;
    //
    // }
    //
    let positions = [...Array(5)].map((_) => {
        return { x: Math.random(), y: Math.random() };
    });
    const translateArray = new Float32Array(positions.length * 2);
    positions.forEach((position, index) => {
        let positionIndex = index * 2;
        translateArray[positionIndex] = position.x;
        translateArray[positionIndex + 1] = position.y;
    });

    const geometry = new THREE.InstancedBufferGeometry();

    geometry.setAttribute(
        "position",

        new THREE.Float32BufferAttribute(position, 2).setUsage(
            THREE.StaticDrawUsage,
        ),
    );
    geometry.setAttribute(
        "translate",
        new THREE.InstancedBufferAttribute(translateArray, 2),
    );

    scene.add(new THREE.Mesh(geometry, material));


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
