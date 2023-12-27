# version 300 es

in vec4 aVertexPosition;


//uniform mat4 uModelViewMatrix;
uniform float aspect;

uniform mat4 uProjectionMatrix;

out vec3 position;

void main() {
    gl_Position = aVertexPosition;
    gl_Position /= aspect;
    position = gl_Position.xyz;
}
