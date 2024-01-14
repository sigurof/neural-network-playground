import { range } from "./utils.ts";


test('range function works', ()=>{
    expect(range(0)).toEqual([])
    expect(range(1)).toEqual([0])
    expect(range(2)).toEqual([0,1])
    expect(range(3)).toEqual([0,1,2])
})
