precision highp float;

in vec2 coord2d;

out vec4 out_Color;

uniform mat3 firstWeights;
uniform mat4 secondWeights;
uniform mat4 thirdWeights;

vec4 elementwiseSigmoid(vec4 v){
    return vec4(
    1.0 / (1.0 + exp(-v.x)),
    1.0 / (1.0 + exp(-v.y)),
    1.0 / (1.0 + exp(-v.z)),
    1.0 / (1.0 + exp(-v.w))
    );
}

void main(void){

//    mat3 firstWeights = mat3(
//    0.0, 0.0, 0.0,
//    0.0, -0.5, 0.0,
//    0.0, 0.0, -0.5
//    );
//
//    mat4 secondWeights = mat4(
//    0.0, 0.0, 0.0, 0.0,
//    0.0, -0.5, 0.0, 0.0,
//    0.0, 0.0, -0.5, 0.0,
//    0.0, 0.0, 0.0, -0.0 // dummy row
//    );
//
//    mat4 thirdWeights = mat4(
//    0.0, 0.0, 0.0, 0.0,
//    0.0, -0.5, 0.0, 0.0,
//    0.0, 0.0, -0.5, 0.0,
//    0.0, 0.0, 0.0, -0.5
//    );

    // First activations
    vec2 firstLayer = coord2d;

    // Second activations
    vec3 secondLayerTemp = firstWeights * vec3(firstLayer, 1);
    vec3 secondLayer = elementwiseSigmoid(vec4(secondLayerTemp, 0)).xyz;

    // Third activations
    vec3 thirdLayerTemp = (secondWeights * vec4(secondLayer, 1)).xyz;
    vec3 thirdLayer = elementwiseSigmoid(vec4(thirdLayerTemp, 0)).xyz;

    // Last activations
    vec3 lastLayerTemp = (thirdWeights * vec4(thirdLayer, 1)).xyz;
    vec3 lastLayer = elementwiseSigmoid(vec4(lastLayerTemp, 0)).xyz;


    out_Color = vec4(lastLayer.r, lastLayer.g, lastLayer.b, 1.0);

    gl_FragDepth = 0.9;
}
