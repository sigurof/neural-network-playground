#version 410 core

in vec2 coord2d;

out vec4 out_Color;

void main(void){
    // Discaring everything outside the sphere surface
    if (dot(coord2d, coord2d) > 1){
        discard;
    } else {
        out_Color = vec4(1, 1, 1, 0);
    }
}
