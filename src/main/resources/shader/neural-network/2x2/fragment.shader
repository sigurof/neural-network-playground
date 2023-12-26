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

    if (result.x > result.y){
        out_Color = vec4(1, 0, 0, 0);
    }else {
        out_Color = vec4(0, 0, 1, 0);
    }

}
