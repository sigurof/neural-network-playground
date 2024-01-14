type CircleData = {
    position: { x: number; y: number };
    color: "red" | "blue";
};

const randomlyDistributedPoints = (numberOfCircles: number) =>
    [...Array(numberOfCircles)].map((_) => ({
        x: (Math.random() - 0.5) * 2,
        y: (Math.random() - 0.5) * 2,
    }));

function redUpperRightLinear(x: number, y: number) {
    return x + y > 0 ? "red" : "blue";
}

function redMiddleCircle(x: number, y: number) {
    return x * x + y * y < 0.5 ? "red" : "blue";
}

type ColorPicker = (x: number, y: number) => "red" | "blue";
const randomlyDistributedPointsSeparatedBy = (
    number: number,
    colorPicker: ColorPicker,
): CircleData[] => {
    return randomlyDistributedPoints(number).map((point) => ({
        position: point,
        color: colorPicker(point.x, point.y),
    }));
};

const BLUE_ARRAY = [0.0, 1.0];
const RED_ARRAY = [1.0, 0.0];
type TrainingPoint = { input: number[]; output: number[] };
export type TrainingData = TrainingPoint[];
const toTrainingData = (circleData: CircleData) => ({
    input: [circleData.position.x, circleData.position.y],
    output: circleData.color === "red" ? RED_ARRAY : BLUE_ARRAY,
});
export const circlesDataSets = {
    linear: {
        variant1: randomlyDistributedPointsSeparatedBy(
            100,
            redUpperRightLinear,
        ).map(toTrainingData),
    },
    nonLinear: {
        circularRegion: randomlyDistributedPointsSeparatedBy(
            100,
            redMiddleCircle,
        ).map(toTrainingData),
    },
};
