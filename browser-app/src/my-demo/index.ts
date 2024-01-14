import { initShaderProgram } from "./webgl-demo.ts";
import { drawScene } from "./draw-scene.ts";
import { initBuffers } from "./init-buffers.ts";
import vtxSource from "./shaders/vertex.shader?raw";
import fragSource from "./shaders/fragment.shader?raw";

export type ProgramInfo = {
    uniformLocations: {
        projectionMatrix: WebGLUniformLocation;
        aspect: WebGLUniformLocation;
    };
    attribLocations: { vertexPosition: number };
    program: WebGLProgram;
};

export async function myDemo() {
    const canvas = document.querySelector("#canvas") as HTMLCanvasElement;
    const gl: WebGL2RenderingContext = canvas.getContext("webgl2")!!;
    if (!gl) {
        throw new Error(
            "Unable to initialize WebGL. Your browser or machine may not support it.",
        );
    }
    gl.clearColor(0.0, 1.0, 0.0, 1.0); // Clear to black, fully opaque
    gl.clear(gl.COLOR_BUFFER_BIT);

    console.log(`The vertex source code is ${vtxSource}`);

    const shaderProgram = initShaderProgram(gl, vtxSource, fragSource);
    const programInfo: ProgramInfo = {
        program: shaderProgram,
        attribLocations: {
            vertexPosition: gl.getAttribLocation(
                shaderProgram,
                "aVertexPosition",
            ),
        },
        uniformLocations: {
            aspect: gl.getUniformLocation(shaderProgram, "aspect")!!,
            projectionMatrix: gl.getUniformLocation(
                shaderProgram,
                "uProjectionMatrix",
            )!!,
        },
    };

    const buffers: { position: WebGLBuffer } = initBuffers(gl);
    drawScene(gl, programInfo, buffers);
}
