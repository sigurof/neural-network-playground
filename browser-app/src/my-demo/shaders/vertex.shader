# version 300 es

in vec4 aVertexPosition;

uniform mat4 uModelViewMatrix;

uniform mat4 uProjectionMatrix;

out vec3 position;

void main() {
    gl_Position = uProjectionMatrix * uModelViewMatrix * aVertexPosition;
    position = gl_Position.xyz;
}
