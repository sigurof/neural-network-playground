import { sigmoid } from "./maths.ts";

test("Sigmoid function squishes things to between 0 and 1.", () => {
    expect(sigmoid(0)).toEqual(0.5);
    expect(sigmoid(-100)).toEqual(expect.closeTo(0, 3));
    expect(sigmoid(100)).toEqual(expect.closeTo(1, 3));
});
