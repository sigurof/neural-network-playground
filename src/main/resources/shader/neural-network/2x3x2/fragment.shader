#version 410 core

uniform mat3 firstWeights; // a 3 by 3 matrix (2w + 1 bias, 2w + 1 bias, 2w + 1 bias)
uniform mat4 secondWeights; // a 2 by 4 matrix (3w + 1 bias, 3w + 1 bias)

in vec2 coord2d;

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
    vec3 secondLayerTemp = firstWeights * vec3(firstLayer, 1);
    vec3 secondLayer = elementwiseSigmoid(vec4(secondLayerTemp, 0)).xyz;

    // Last Layer
    vec2 lastLayerTemp = (secondWeights * vec4(secondLayer, 1)).xy;
    vec2 lastLayer = elementwiseSigmoid(vec4(lastLayerTemp, 0, 0)).xy;

    out_Color = 0.7*vec4(lastLayer.x, 0.0, lastLayer.y, 0);


}
