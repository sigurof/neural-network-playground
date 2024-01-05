
in vec2 position;
in vec2 translate;
in vec3 colorInstanced;

uniform float aspect;
uniform float radius;
uniform vec2 center;
uniform vec3 color;


out vec2 coord2d;
out vec3 fragColor;


void main(void){
    coord2d = position;
    gl_Position = vec4(translate + radius * coord2d, 0.0, 1.0);
    gl_Position.x /= aspect;
    fragColor = colorInstanced;
}
