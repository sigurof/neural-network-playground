precision highp float;

in vec2 position;
in vec2 translate;

uniform float aspect;

out vec2 coord2d;

void main(void){
    coord2d = position;
    gl_Position = vec4(position + translate, 0.0, 1.0);
}
