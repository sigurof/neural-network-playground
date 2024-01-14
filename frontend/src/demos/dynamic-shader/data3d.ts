import { randomlyDistributedPoints } from "./data.ts";
import { range } from "../../common/utils/utils.ts";

const RED_ARRAY = [1, 0, 0];
const GREEN_ARRAY = [0, 1, 0];
const BLUE_ARRAY = [0, 0, 1];

const radial = (
    increment: number,
    maxIncrements: number,
    startAngle: number,
    color: "red" | "blue",
) => {
    const revolutions = 2
    const finalAngle = revolutions * Math.PI * 2
    const howFarWeAre = increment / (maxIncrements - 1)
    const angle = howFarWeAre * finalAngle + startAngle;
    const r = howFarWeAre;
    const position = {
        x: Math.cos(angle) * r,
        y: Math.sin(angle) * r,
    };
    // const color = "red";
    return {
        color,
        position,
    };
};

export const circlesDataSets = {
    abc: {
        redAndBlue: [
            ...range(50)
                .map((i) => radial(i, 50, Math.PI, "red"))
                .map((point) => {
                    return {
                        input: [point.position.x, point.position.y],
                        output: {
                            red: [1, 0],
                            green: [1, 1],
                            blue: [0, 1],
                        }[point.color] ?? [0, 0, 0],
                    };
                }),

            ...range(50)
                .map((i) => radial(i, 50, 0, "blue"))
                .map((point) => {
                    return {
                        input: [point.position.x, point.position.y],
                        output: {
                            red: [1, 0],
                            green: [1, 1],
                            blue: [0, 1],
                        }[point.color] ?? [0, 0, 0],
                    };
                }),
        ],
    },
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
