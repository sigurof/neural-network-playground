# version 300 es

precision highp float;

in vec3 position;
in vec3 worldPosition;

out vec4 fragColor;

void main() {
    vec3 p = worldPosition;
    fragColor = vec4(p.x*p.x + p.y*p.y, 0, p.z*p.z, 1.0);
}
