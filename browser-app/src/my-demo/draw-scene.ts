export function drawScene(
    gl: WebGL2RenderingContext,
    programInfo: {
        uniformLocations: {
            aspect: WebGLUniformLocation;
        };
        attribLocations: { vertexPosition: number };
        program: WebGLProgram;
    },
    circleProgramInfo: {
        uniformLocations: {
            aspect: WebGLUniformLocation;
            color: WebGLUniformLocation;
            radius: WebGLUniformLocation;
            center: WebGLUniformLocation;
        };
        program: WebGLProgram;
    },
    buffers: { position: WebGLBuffer; vao: WebGLVertexArrayObject },
) {
    gl.clearColor(0.0, 0.0, 0.0, 1.0);
    gl.clearDepth(1.0);
    gl.enable(gl.DEPTH_TEST);
    gl.depthFunc(gl.LEQUAL);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    let canvas = gl.canvas;
    if (!(canvas instanceof HTMLCanvasElement)) {
        throw Error("canvas is not an HTMLCanvasElement");
    }
    const aspect = canvas.clientWidth / canvas.clientHeight;
    setPositionAttribute(gl, buffers, programInfo);
    gl.useProgram(programInfo.program);
    gl.uniform1f(programInfo.uniformLocations.aspect, aspect);
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    gl.disableVertexAttribArray(programInfo.attribLocations.vertexPosition);
    gl.bindVertexArray(null);
    gl.useProgram(null);


    gl.useProgram(circleProgramInfo.program);
    gl.bindVertexArray(buffers.vao);
    for (let point of [
        { x: 0, y: 0 },
        { x: 0, y: 1 },
        { x: 1, y: 0 },
        { x: 1, y: 1 },
        { x: 0.5, y: 0.5 },
    ]) {
        console.log(`Drawing circle at ${point.x}, ${point.y}`);
        gl.uniform1f(circleProgramInfo.uniformLocations.aspect, aspect);
        gl.uniform1f(circleProgramInfo.uniformLocations.radius, 0.1);
        gl.uniform2f(
            circleProgramInfo.uniformLocations.center,
            point.x,
            point.y,
        );
        gl.uniform3f(circleProgramInfo.uniformLocations.color, 0.0, 1.0, 0.0);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    }
    gl.useProgram(null);
    gl.bindVertexArray(null);
}

function setPositionAttribute(
    gl: WebGL2RenderingContext,
    buffers: {
        position: WebGLBuffer;
        vao: any;
    },
    programInfo: {
        uniformLocations: {
            aspect: WebGLUniformLocation;
        };
        attribLocations: { vertexPosition: number };
        program: WebGLProgram;
    },
) {
    const numComponents = 2; // pull out 2 values per iteration
    const type = gl.FLOAT; // the data in the buffer is 32bit floats
    const normalize = false; // don't normalize
    const stride = 0; // how many bytes to get from one set of values to the next
    const offset = 0; // how many bytes inside the buffer to start from
    gl.bindBuffer(gl.ARRAY_BUFFER, buffers.position);
    gl.vertexAttribPointer(
        programInfo.attribLocations.vertexPosition,
        numComponents,
        type,
        normalize,
        stride,
        offset,
    );
    gl.enableVertexAttribArray(programInfo.attribLocations.vertexPosition);
}
