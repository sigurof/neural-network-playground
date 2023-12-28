uniform float aspect;
uniform float radius;
uniform vec2 center;
uniform vec3 color;

attribute vec2 position;

varying vec2 coord2d;
varying vec3 fragColor;

//vec2 whichVertex() {
//    vec2 coord2dOut;
//
//    if (gl_VertexID == 0) {
//        coord2dOut = vec2(-1.0, 1.0);
//    } else if (gl_VertexID == 1) {
//        coord2dOut = vec2(-1.0, -1.0);
//    } else if (gl_VertexID == 2) {
//        coord2dOut = vec2(1.0, 1.0);
//    } else if (gl_VertexID == 3) {
//        coord2dOut = vec2(1.0, -1.0);
//    }
//
//    return coord2dOut;
//}

void main(void) {
    coord2d = position;
    gl_Position = vec4(center + radius * coord2d, 0.0, 1.0);
    gl_Position.x /= aspect;
    fragColor = color;
}
