document.addEventListener("DOMContentLoaded", function () {
    const checkAll = document.getElementById("checkAllOrders");
    const checkboxes = document.querySelectorAll(".order-checkbox:not(:disabled)");
    const confirmForms = document.querySelectorAll(".confirm-form");

    if (checkAll) {
        checkAll.addEventListener("change", function () {
            checkboxes.forEach(cb => {
                cb.checked = checkAll.checked;
            });
        });
    }

    confirmForms.forEach(form => {
        form.addEventListener("submit", function (e) {
            const message = form.getAttribute("data-message") || "Bạn chắc chắn muốn thực hiện?";

            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });
});