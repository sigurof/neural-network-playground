import { randomlyDistributedPoints } from "./data.ts";

const RED_ARRAY = [1, 0, 0];
const GREEN_ARRAY = [0, 1, 0];
const BLUE_ARRAY = [0, 0, 1];

export const circlesDataSets = {
    nonLinear: {
        redGreenBlue: randomlyDistributedPoints(100)
            .map((point) => {
                const cosineColor = [
                    { color: "red", value: Math.cos(point.x) },
                    { color: "green", value: Math.cos(point.y) },
                    { color: "blue", value: Math.sin(point.x) },
                ];
                cosineColor.sort(
                    (a, b) => Math.abs(b.value) - Math.abs(a.value),
                );
                return {
                    position: point,
                    color: cosineColor[0].color,
                };
            })
            .map((point) => {
                return {
                    input: [point.position.x, point.position.y],
                    output: {
                        red: RED_ARRAY,
                        green: GREEN_ARRAY,
                        blue: BLUE_ARRAY,
                    }[point.color] ?? [0, 0, 0],
                };
            }),
    },
};
