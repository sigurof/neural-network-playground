
in vec2 position;

uniform float aspect;
uniform float radius;
uniform vec2 center;
uniform vec3 color;


out vec2 coord2d;
out vec3 fragColor;


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
    coord2d = position;
    gl_Position = vec4(center + radius * coord2d, 0.0, 1.0);
    gl_Position.x /= aspect;
    fragColor = color;
}
