#version 410 core

in vec2 coord2d;
in mat3 fragMatrix;

out vec4 out_Color;


vec2 elementwiseSigmoid(vec2 v){
    return vec2(
        1.0 / (1.0 + exp(-v.x)),
        1.0 / (1.0 + exp(-v.y))
    );
}

void main(void){
    vec3 xy1 = vec3(coord2d, 1.0);
    vec2 result = elementwiseSigmoid((fragMatrix * xy1).xy);
    out_Color = 0.7*vec4(result.x, 0.0, result.y, 0);
}
