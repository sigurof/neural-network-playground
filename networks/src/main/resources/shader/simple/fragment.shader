#version 410 core

in vec2 coord2d;
in vec3 fragColor;

out vec4 out_Color;

void main(void){
    // Discaring everything outside the sphere surface
    if (dot(coord2d, coord2d) > 1){
        discard;
    } else {
        out_Color = vec4(fragColor, 0);
    }
}
