import { Chart, registerables } from "chart.js";

export let chartInitialized = false;

export const startChartJs = () => {
    chartInitialized = true;
    console.log("startChartJs");
    const ctx = document.getElementById("chartCanvas");
    if (!ctx) {
        return;
    }

    Chart.register(...registerables);
    // @ts-ignore
    new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
            datasets: [
                {
                    label: "# of Votes",
                    data: [12, 19, 3, 5, 2, 3],
                    borderWidth: 1,
                },
            ],
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                },
            },
        },
    });
};
