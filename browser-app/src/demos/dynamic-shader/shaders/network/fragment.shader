precision highp float;

in vec2 coord2d;

out vec4 out_Color;

uniform float[50] allWeightsAndBiases;
uniform int numberOfMatrices;
uniform ivec2[5] matrixDimensions;

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

float[50] flatMatrix(float[50] allMatricesFlat, int currentMatrixIndex, ivec2[5] allMatrixDimensions){
    int offset = 0;
    for (int i = 0; i < currentMatrixIndex; i++){
        offset += allMatrixDimensions[i].x * allMatrixDimensions[i].y;
    }
    int currentMatrixSize = allMatrixDimensions[currentMatrixIndex].x * allMatrixDimensions[currentMatrixIndex].y;
    float[50] currentMatrix;
    for (int i = 0; i < currentMatrixSize; i++){
        currentMatrix[i] = allMatricesFlat[offset + i];
    }
    // set the rest to 0
    for (int i = currentMatrixSize; i < 50; i++){
        currentMatrix[i] = 0.0;
    }
    return currentMatrix;
}

float element(float[50] matrix, ivec2 matrixDimensions, int row, int col){
    return matrix[row * matrixDimensions.y + col];
}

float[30] matrixMult(float[50] matrix, ivec2 matrixDimensions, vec2 firstLayer){
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

float[30] matrixMult(float[50] matrix, ivec2 matrixDimensions, float[30] vector){
    float[30] result;
    vector[matrixDimensions.y - 1] = 1.0;
    for (int i = 0; i < matrixDimensions.x; i++){
        float sum = 0.0;
        for (int j = 0; j < matrixDimensions.y; j++){
            sum += element(matrix, matrixDimensions, i, j) * vector[j];
        }
        result[i] = sum;
    }
    // set the rest to zero
    for (int i = matrixDimensions.x; i < 30; i++){
        result[i] = 0.0;
    }
    return result;
}

void main(void){

    // First Layer
    vec2 firstLayer = coord2d;

    // Second Layer (special treatment because uses vec2 type that comes from vertex shader instead of float[])

    float[50] firstWeights = flatMatrix(allWeightsAndBiases, 0, matrixDimensions);
    float[30] nextLayerActivation = matrixMult(firstWeights, matrixDimensions[0], firstLayer);// zfirstWeights * vec3(firstLayer, 1);
    nextLayerActivation = elementwiseSigmoid(nextLayerActivation, matrixDimensions[0].x);

    // Then iterate over all the remaining layers
    float[50] nextWeights;
//    float[30] nextLayerZ;
//    int remainingWeightMatrices = numberOfMatrices -1;
    for (int i = 1; i < numberOfMatrices; i++){
        nextWeights = flatMatrix(allWeightsAndBiases, i, matrixDimensions);
        nextLayerActivation = matrixMult(nextWeights, matrixDimensions[i], nextLayerActivation);
        nextLayerActivation = elementwiseSigmoid(nextLayerActivation, matrixDimensions[i].x);
    }

    vec3  lastLayer = vec3(nextLayerActivation[0], 0, nextLayerActivation[1]);
    out_Color = 0.8*vec4(lastLayer.r, lastLayer.g, lastLayer.b, 1.0);

    gl_FragDepth = 0.9;
}
