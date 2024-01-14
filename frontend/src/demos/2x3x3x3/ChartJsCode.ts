import { Chart, ChartConfiguration, ChartData, registerables } from "chart.js";
import { range } from "../../common/utils/utils.ts";
// import { ChartConfiguration } from "@types/chart.js";

export let chartInitialized = false;

export const startChartJs = () => {
    chartInitialized = true;
    console.log("startChartJs");
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
    console.log("labels", labels);
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
            maintainAspectRatio: false,
            responsive: true,
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
    const chart = new Chart(ctx, config);

    const updateChart = (points: { x: number; y: number }[]) => {
        console.log("updateChart", points);
        chart.data.datasets[0].data = points;
        chart.data.labels = points.map((d) => `${d.x}`);
        chart.update();
    };
    return {
        updateChart,
    };
};
