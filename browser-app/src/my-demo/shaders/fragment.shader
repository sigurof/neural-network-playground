# version 300 es

precision highp float;

in vec3 position;
out vec4 fragColor;

void main() {
    vec3 p = position;
    fragColor = vec4(p.x*p.x + p.y*p.y, 0, p.z*p.z, 1.0);
}
