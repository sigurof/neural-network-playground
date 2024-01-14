#version 410 core

in vec2 coord2d;
in mat3 fragFirstWeights; // a 3 by 3 matrix (2w + 1 bias, 2w + 1 bias, 2w + 1 bias)
in mat4 fragSecondWeights;// a 2 by 4 matrix (3w + 1 bias, 3w + 1 bias)

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

    // First Layer
    vec2 firstLayer = coord2d;

    // Middle Layer
    vec3 secondLayerTemp = fragFirstWeights * vec3(firstLayer, 1);
    vec3 secondLayer = elementwiseSigmoid(vec4(secondLayerTemp, 0)).xyz;

    // Last Layer
    vec2 lastLayerTemp = (fragSecondWeights * vec4(secondLayer, 1)).xy;
    vec2 lastLayer = elementwiseSigmoid(vec4(lastLayerTemp, 0, 0)).xy;

    out_Color = 0.7*vec4(lastLayer.x, 0.0, lastLayer.y, 0);


}
