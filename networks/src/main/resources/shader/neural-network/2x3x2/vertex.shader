#version 410 core

uniform float aspectRatio;
uniform mat3 firstWeights;
uniform mat4 secondWeights;

out vec2 coord2d;


vec2 whichVertex(){
    vec2 coord2dOut;
    switch (gl_VertexID){
        case 0:
        coord2dOut = vec2(-1.0, 1.0);
        break;
        case 1:
        coord2dOut = vec2(-1.0, -1.0);
        break;
        case 2:
        coord2dOut = vec2(1.0, 1.0);
        break;
        case 3:
        coord2dOut = vec2(1.0, -1.0);
        break;
    }
    return coord2dOut;
}

void main(void){
    coord2d = whichVertex();
    gl_Position = vec4(coord2d, 0.0, 1.0);
    gl_Position.x /= aspectRatio;
}
