precision highp float;

//#define MATRIX_DIMS_MAX_SIZE = 10
//uniform vec3 weightsAndBiases[MAX_PARAMS];

//uniform int numParams;
in vec2 coord2d;

out vec4 out_Color;

uniform mat3 firstWeights;// a 3 by 3 matrix (2w + 1 bias, 2w + 1 bias, 2w + 1 bias)
uniform mat4 secondWeights;// a 2 by 4 matrix (3w + 1 bias, 3w + 1 bias)

vec4 elementwiseSigmoid(vec4 v){
    return vec4(
    1.0 / (1.0 + exp(-v.x)),
    1.0 / (1.0 + exp(-v.y)),
    1.0 / (1.0 + exp(-v.z)),
    1.0 / (1.0 + exp(-v.w))
    );
}

float[30] elementwiseSigmoid(float[30] v, int rows){
    float[30] result;
    for (int i = 0; i < rows; i++){
        result[i] = 1.0 / (1.0 + exp(-v[i]));
    }
    return result;
}

float[100] flatMatrix(float[20] allMatricesFlat, int currentMatrixIndex, ivec2[10] allMatrixDimensions){
    int offset = 0;
    for (int i = 0; i < currentMatrixIndex; i++){
        offset += allMatrixDimensions[i].x * allMatrixDimensions[i].y;
    }
    int currentMatrixSize = allMatrixDimensions[currentMatrixIndex].x * allMatrixDimensions[currentMatrixIndex].y;
    float[100] currentMatrix;
    for (int i = 0; i < currentMatrixSize; i++){
        currentMatrix[i] = allMatricesFlat[offset + i];
    }
    // set the rest to 0
    for (int i = currentMatrixSize; i < 100; i++){
        currentMatrix[i] = 0.0;
    }
    return currentMatrix;
}

float element(float[100] matrix, ivec2 matrixDimensions, int row, int col){
    return matrix[row * matrixDimensions.y + col];
}

float[30] matrixMult(float[100] matrix, ivec2 matrixDimensions, vec2 firstLayer){
    float[3] vector = float[3](firstLayer.x, firstLayer.y, 1.0);
    float[30] result;
    // Set all elements to 0 first
    for (int i = 0; i < 30; i++){
        result[i] = 0.0;
    }
    for (int i = 0; i < matrixDimensions.x; i++){
        float sum = 0.0;
        for (int j = 0; j < matrixDimensions.y; j++){
            sum += element(matrix, matrixDimensions, i, j) * vector[j];
        }
        result[i] = sum;
    }
    return result;
}

float[30] matrixMult(float[100] matrix, ivec2 matrixDimensions, float[30] vector){
    float[30] result;
    vector[matrixDimensions.y - 1] = 1.0;
    for (int i = 0; i < matrixDimensions.x; i++){
        float sum = 0.0;
        for (int j = 0; j < matrixDimensions.y; j++){
            sum += element(matrix, matrixDimensions, i, j) * vector[j];
        }
        result[i] = sum;
    }
    return result;
}

void main(void){
    ivec2[10] matrixDimensions;
    matrixDimensions[0] = ivec2(3, 3);
    matrixDimensions[1] = ivec2(3, 4);


    // matrices are column-major, so need to take them in transposed order
    float[20] allWeightsAndBiases = float[](
    firstWeights[0][0], firstWeights[1][0], firstWeights[2][0],
    firstWeights[0][1], firstWeights[1][1], firstWeights[2][1],
    firstWeights[0][2], firstWeights[1][2], firstWeights[2][2],
    secondWeights[0][0], secondWeights[1][0], secondWeights[2][0], secondWeights[3][0],
    secondWeights[0][1], secondWeights[1][1], secondWeights[2][1], secondWeights[3][1],
    0.0, 0.0, 0.0
    );


    // First Layer
    vec2 firstLayer = coord2d;

    // Middle Layer
    float[100] firstWeights = flatMatrix(allWeightsAndBiases, 0, matrixDimensions);
    float[30] secondLayerZ = matrixMult(firstWeights, matrixDimensions[0], firstLayer);// zfirstWeights * vec3(firstLayer, 1);
    float[30] secondLayerActivation = elementwiseSigmoid(secondLayerZ, matrixDimensions[0].y);

    // Last Layer
    float[100] secondWeights = flatMatrix(allWeightsAndBiases, 1, matrixDimensions);
    float[30] lastLayerZ = matrixMult(secondWeights, matrixDimensions[1], secondLayerActivation);
    float[30] lastLayerActivation = elementwiseSigmoid(lastLayerZ, matrixDimensions[1].x);
    vec3  lastLayer = vec3(lastLayerActivation[0], 0, lastLayerActivation[1]);

    out_Color = 0.8*vec4(lastLayer.r, lastLayer.g, lastLayer.b, 1.0);

    gl_FragDepth = 0.9;
}
