# version 300 es

in vec4 aVertexPosition;

uniform float aspect;

out vec3 position;
out vec3 worldPosition;

void main() {
    gl_Position = aVertexPosition;
    worldPosition = gl_Position.xyz;
    worldPosition.x *= aspect;
    position = gl_Position.xyz;
}
