import { initShaderProgram } from "./webgl-demo.ts";
import { drawScene } from "./draw-scene.ts";
import { initBuffers } from "./init-buffers.ts";
import vtxSource from "./shaders/network/vertex.shader?raw";
import fragSource from "./shaders/network/fragment.shader?raw";
import circlesVtxSource from "./shaders/circles/vertex.shader?raw";
import circlesFragSource from "./shaders/circles/fragment.shader?raw";

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

    const networkShader = initShaderProgram(gl, vtxSource, fragSource);
    const circlesShader = initShaderProgram(
        gl,
        circlesVtxSource,
        circlesFragSource,
    );
    const programInfo: {
        uniformLocations: {
            aspect: WebGLUniformLocation;
        };
        attribLocations: { vertexPosition: number };
        program: WebGLProgram;
    } = {
        program: networkShader,
        attribLocations: {
            vertexPosition: gl.getAttribLocation(
                networkShader,
                "aVertexPosition",
            ),
        },
        uniformLocations: {
            aspect: gl.getUniformLocation(networkShader, "aspect")!!,
        },
    };
    const programInfoCircles: {
        uniformLocations: {
            aspect: WebGLUniformLocation;
            color: WebGLUniformLocation;
            radius: WebGLUniformLocation;
            center: WebGLUniformLocation;
        };
        program: WebGLProgram;
    } = {
        program: circlesShader,
        uniformLocations: {
            aspect: gl.getUniformLocation(circlesShader, "aspect")!!,
            color: gl.getUniformLocation(circlesShader, "color")!!,
            radius: gl.getUniformLocation(circlesShader, "radius")!!,
            center: gl.getUniformLocation(circlesShader, "center")!!,
        },
    };

    const buffers: { position: WebGLBuffer; vao: WebGLVertexArrayObject } =
        initBuffers(gl);
    drawScene(gl, programInfo, programInfoCircles, buffers);
}
