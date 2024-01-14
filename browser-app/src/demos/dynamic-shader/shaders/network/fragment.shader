precision highp float;

const int MAX_LAYERS = 21;
const int MAX_VEC_SIZE = MAX_LAYERS - 1;
const int MAX_MATRIX_SIZE = 100;
const int MAX_MATRICES = 10;

in vec2 coord2d;

out vec4 out_Color;

uniform float[MAX_MATRIX_SIZE] allWeightsAndBiases;
uniform int numberOfMatrices;
uniform ivec2[MAX_MATRICES] matrixDimensions;

vec4 elementwiseSigmoid(vec4 v){
    return vec4(
    1.0 / (1.0 + exp(-v.x)),
    1.0 / (1.0 + exp(-v.y)),
    1.0 / (1.0 + exp(-v.z)),
    1.0 / (1.0 + exp(-v.w))
    );
}

float[MAX_VEC_SIZE] elementwiseSigmoid(float[MAX_VEC_SIZE] vector, int rows){
    float[MAX_VEC_SIZE] result;
    for (int i = 0; i < rows; i++){
        result[i] = 1.0 / (1.0 + exp(-vector[i]));
    }
    return result;
}

int flatMatrix(int currentMatrixIndex, ivec2[MAX_MATRICES] allMatrixDimensions){
    int offset = 0;
    for (int i = 0; i < currentMatrixIndex; i++){
        offset += allMatrixDimensions[i].x * allMatrixDimensions[i].y;
    }
    return offset;
}

float[MAX_VEC_SIZE] matrixMult(float[MAX_MATRIX_SIZE] allMatrixData, int thisMatrixStartIndex, ivec2 matrixDimensions, float[MAX_VEC_SIZE] vector){
    float[MAX_VEC_SIZE] result;
    vector[matrixDimensions.y - 1] = 1.0;
    for (int i = 0; i < matrixDimensions.x; i++){
        for (int j = 0; j < matrixDimensions.y; j++){
            float matrixValue = allMatrixData[thisMatrixStartIndex + i * matrixDimensions.y + j];
            result[i] += matrixValue * vector[j];
        }
    }
    // set the rest to zero
    for (int i = matrixDimensions.x; i < MAX_VEC_SIZE; i++){
        result[i] = 0.0;
    }
    return result;
}

void main(void){

    // Building first Layer vector
    vec2 firstLayer = coord2d;
    float[MAX_VEC_SIZE] nextLayerActivation;
    int nextWeightsStartIndex;
    nextLayerActivation[0] = firstLayer.x;
    nextLayerActivation[1] = firstLayer.y;

    for (int i = 0; i < numberOfMatrices; i++){
        nextWeightsStartIndex = flatMatrix(i, matrixDimensions);
        nextLayerActivation = matrixMult(allWeightsAndBiases, nextWeightsStartIndex, matrixDimensions[i], nextLayerActivation);
        nextLayerActivation = elementwiseSigmoid(nextLayerActivation, matrixDimensions[i].x);
    }

    vec3  lastLayer = vec3(nextLayerActivation[0], 0, nextLayerActivation[1]);
    out_Color = 0.8 * vec4(lastLayer.r, lastLayer.g, lastLayer.b, 1.0);

    gl_FragDepth = 0.9;
}
