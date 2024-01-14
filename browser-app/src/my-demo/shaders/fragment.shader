# version 300 es

precision highp float;

in vec3 position;
out vec4 fragColor;

void main() {
    fragColor = vec4(position, 1.0);
}
