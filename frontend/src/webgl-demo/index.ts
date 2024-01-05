import { initShaderProgram } from "./webgl-demo.ts";
import { drawScene } from "./draw-scene.ts";
import { initBuffers } from "./init-buffers.ts";
// vtxSource and fragSource are defined public/shaders/webgl-demo as vertex.shader and fragment.shader
import vertexShaderUrl from "/shaders/webgl-demo/vertex.shader?url";
import fragShaderUrl from "/shaders/webgl-demo/fragment.shader?url";

export type ProgramInfo = {
    uniformLocations: {
        projectionMatrix: WebGLUniformLocation;
        modelViewMatrix: WebGLUniformLocation;
    };
    attribLocations: { vertexPosition: number };
    program: WebGLProgram;
};

export async function webGlDemo() {
    const vtxSource: string = await fetch(vertexShaderUrl).then((response) =>
        response.text(),
    );
    const fragSource: string = await fetch(fragShaderUrl).then((response) =>
        response.text(),
    );
    const canvas = document.querySelector("#canvas") as HTMLCanvasElement;
    const gl: WebGL2RenderingContext = canvas.getContext("webgl2")!!;
    if (!gl) {
        throw Error(
            "Unable to initialize WebGL. Your browser or machine may not support it.",
        );
    }
    gl.clearColor(0.0, 1.0, 0.0, 1.0); // Clear to black, fully opaque
    gl.clear(gl.COLOR_BUFFER_BIT);

    console.log(`The vertex source code is ${vtxSource}`);

    const shaderProgram = initShaderProgram(gl, vtxSource, fragSource);
    // Collect all the info needed to use the shader program.
    // Look up which attribute our shader program is using
    // for aVertexPosition and look up uniform locations.
    const programInfo: ProgramInfo = {
        program: shaderProgram,
        attribLocations: {
            vertexPosition: gl.getAttribLocation(
                shaderProgram,
                "aVertexPosition",
            ),
        },
        uniformLocations: {
            projectionMatrix: gl.getUniformLocation(
                shaderProgram,
                "uProjectionMatrix",
            )!!,
            modelViewMatrix: gl.getUniformLocation(
                shaderProgram,
                "uModelViewMatrix",
            )!!,
        },
    };

    // Here's where we call the routine that builds all the
    // objects we'll be drawing.
    const buffers: { position: WebGLBuffer } = initBuffers(gl);

    // Draw the scene
    drawScene(gl, programInfo, buffers);
}
