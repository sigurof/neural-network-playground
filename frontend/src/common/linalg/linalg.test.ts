import { identityMatrix, matrixMultiplication } from "./linalg.ts";

describe('matrix multiplication', ()=>{

    it('identity matrix preserves vector', ()=>{
        const identityMatrix2x2 = identityMatrix(2);
        const inVector = [1, 2];
        expect(matrixMultiplication(identityMatrix2x2, inVector)).toEqual(inVector);
    })

    it('3x3 test case', ()=>{
        const matrix3x3 = [
            [1, 2, 3], // 1 + 4 + 9 = 14
            [4, 5, 6], // 4 + 10 + 18 = 32
            [7, 8, 9], // 7 + 16 + 27 = 50
        ];
        const vector3 = [1, 2, 3];
        expect(matrixMultiplication(matrix3x3, vector3)).toEqual([14, 32, 50]);
    })

    it('fails if inputs have wrong dimensions in relation to each other', ()=>{
        expect(()=>matrixMultiplication([[1, 2, 3]], [1, 2])).toThrow();
    })
})
