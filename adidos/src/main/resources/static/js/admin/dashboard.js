document.addEventListener("DOMContentLoaded", function () {
    if (!window.dashboardData) return;

    renderRevenueChart();
    renderPaymentChart();
});

function renderRevenueChart() {
    const chartElement = document.getElementById("monthlyRevenueChart");

    if (!chartElement) return;

    const labels = window.dashboardData.chartLabels || [];
    const revenue = (window.dashboardData.chartRevenue || []).map(value => Number(value));

    new Chart(chartElement, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "Doanh thu",
                    data: revenue
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    ticks: {
                        callback: function (value) {
                            return formatMoney(value);
                        }
                    }
                }
            }
        }
    });
}

function renderPaymentChart() {
    const chartElement = document.getElementById("paymentMethodChart");

    if (!chartElement) return;

    const labels = window.dashboardData.paymentLabels || [];
    const data = (window.dashboardData.paymentData || []).map(value => Number(value));

    new Chart(chartElement, {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "Số đơn",
                    data: data
                }
            ]
        },
        options: {
            responsive: true
        }
    });
}

function formatMoney(value) {
    return new Intl.NumberFormat("vi-VN").format(value) + "đ";
}