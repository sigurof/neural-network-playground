
precision highp float;

in vec2 coord2d;
in vec3 fragColor;

out vec4 out_Color;

void main(void){
    if (dot(coord2d, coord2d) > 1.0){
        discard;
    } else {
        out_Color = vec4(fragColor, 1);
    }
}
