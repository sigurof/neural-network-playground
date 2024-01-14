import { Chart, ChartConfiguration, ChartData, registerables } from "chart.js";
import { range } from "../../common/utils/utils.ts";
import { useEffect, useRef } from "react";
// import { ChartConfiguration } from "@types/chart.js";

export let chartInitialized = false;

export const startChartJs = () => {
    chartInitialized = true;
    const ctx = document.getElementById("chartCanvas");
    if (!ctx) {
        return;
    }

    Chart.register(...registerables);
    const points: { x: number; y: number }[] = range(0).map((i) => ({
        x: i,
        y: i * i,
    }));
    const labels: string[] = points.map((d) => `${d.x}`);
    const data: ChartData = {
        labels: labels,
        datasets: [
            {
                label: "My First Dataset",
                data: points,
                pointStyle: "line",
                fill: false,
                borderColor: "rgb(75, 192, 192)",
                tension: 0.1,
            },
        ],
    };

    const config: ChartConfiguration = {
        type: "line",
        data: data,
        options: {
            aspectRatio: 1,
            maintainAspectRatio: false,
            responsive: false,
            plugins: {
                legend: {
                    position: "top",
                },
                title: {
                    display: true,
                    // text: 'Chart.js Line Chart'
                },
            },
        },
    };
    // @ts-expect-error Expected
    const chart = new Chart(ctx, config);
    return {
        updateChart: (points: { x: number; y: number }[]) => {
            chart.data.datasets[0].data = points;
            chart.data.labels = points.map((d) => `${d.x}`);
            chart.update();
        },
        destroy: () => chart.destroy(),
    };
};

type ChartControls = {
    updateChart: (points: { x: number; y: number }[]) => void;
    destroy: () => void;
};
export const useCharts = () => {
    const chartControls = useRef<ChartControls | null>(null);
    useEffect(() => {
        return () => {
            chartControls.current?.destroy();
        };
    }, []);

    useEffect(() => {
        if (!chartInitialized) {
            chartControls.current = {
                ...startChartJs()!,
            };
        }
    }, []);
    return { chartControls };
};
