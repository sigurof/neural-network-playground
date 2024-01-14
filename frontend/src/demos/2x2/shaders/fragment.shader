precision highp float;

in vec2 coord2d;

uniform mat3 fragMatrix;

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
    vec3 actualResult = vec3(result.x, 0.0, result.y);
    vec3 hardBoundary;
    if (result.x > result.y){
        hardBoundary= 0.7*vec4(1.0, 0.0, 0.0, 1.0).xyz;
    }
    else{
        hardBoundary= 0.7*vec4(0.0, 0.0, 1.0, 1.0).xyz;
    }
    out_Color = vec4(actualResult.x, 0.0, actualResult.z, 1.0);
//    out_Color = vec4()
    gl_FragDepth = 0.9;
}
