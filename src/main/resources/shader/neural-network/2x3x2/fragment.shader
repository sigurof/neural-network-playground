#version 410 core

in vec2 coord2d;
in mat3 fragFirstWeights;// 3 by 3
in mat4 fragSecondWeights;// 2 by 4

out vec4 out_Color;

vec4 elementwiseSigmoid(vec4 v){
    return vec4(
    1.0 / (1.0 + exp(-v.x)),
    1.0 / (1.0 + exp(-v.y)),
    1.0 / (1.0 + exp(-v.z)),
    1.0 / (1.0 + exp(-v.w))
    );
}

void main(void){

    vec2 firstLayer = coord2d;

    vec3 secondLayerTemp = fragFirstWeights * vec3(firstLayer, 1);
    vec3 secondLayer = elementwiseSigmoid(vec4(secondLayerTemp, 0)).xyz;

    vec2 lastLayerTemp = (fragSecondWeights * vec4(secondLayer, 1)).xy;
    vec2 lastLayer = elementwiseSigmoid(vec4(lastLayerTemp, 0, 0)).xy;

    //         out_Color = vec4(result.x, 0, 0, 0);
    out_Color = 0.7*vec4(lastLayer.x, 0.0, lastLayer.y, 0);
    //     if (result.x > result.y){
    //         out_Color = vec4(result, 0.0, 0);
    //     }else {
    //         out_Color = vec4(result, 0.0, 0);
    //     }

}
