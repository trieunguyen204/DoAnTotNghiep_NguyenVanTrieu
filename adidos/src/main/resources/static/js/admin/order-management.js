document.addEventListener("DOMContentLoaded", function () {

    // =========================
    // CHECK ALL ORDERS
    // =========================
    const checkAll = document.getElementById("checkAllOrders");
    const orderCheckboxes = document.querySelectorAll(".order-checkbox");

    if (checkAll) {
        checkAll.addEventListener("change", function () {
            orderCheckboxes.forEach(cb => {
                if (!cb.disabled) {
                    cb.checked = checkAll.checked;
                }
            });
        });
    }

    // Nếu bỏ tick từng cái -> bỏ tick check all
    orderCheckboxes.forEach(cb => {
        cb.addEventListener("change", function () {
            const enabledCheckboxes = [...orderCheckboxes].filter(item => !item.disabled);
            const checkedCheckboxes = enabledCheckboxes.filter(item => item.checked);

            if (checkAll) {
                checkAll.checked =
                    enabledCheckboxes.length > 0 &&
                    enabledCheckboxes.length === checkedCheckboxes.length;
            }
        });
    });


    // =========================
    // CONFIRM FORM ACTIONS
    // =========================
    const confirmForms = document.querySelectorAll(".confirm-form");

    confirmForms.forEach(form => {
        form.addEventListener("submit", function (e) {
            const message =
                form.getAttribute("data-message") ||
                "Bạn chắc chắn muốn thực hiện thao tác này?";

            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });


    // =========================
    // VALIDATE BULK APPROVE
    // =========================
    const bulkApproveForm = document.getElementById("bulkApproveForm");

    if (bulkApproveForm) {
        bulkApproveForm.addEventListener("submit", function (e) {
            const checkedOrders = document.querySelectorAll(
                ".order-checkbox:checked"
            );

            if (checkedOrders.length === 0) {
                e.preventDefault();
                alert("Vui lòng chọn ít nhất một đơn hàng để duyệt.");
            }
        });
    }


    // =========================
    // AUTO CONFIRM UPDATE STATUS
    // =========================
    const statusForms = document.querySelectorAll(".status-update-form");

    statusForms.forEach(form => {
        form.addEventListener("submit", function (e) {
            const select = form.querySelector(".status-select");

            if (!select) return;

            const selectedStatus = select.value;

            const confirmMessage =
                "Bạn có chắc muốn cập nhật trạng thái đơn hàng thành: " +
                selectedStatus +
                " ?";

            if (!confirm(confirmMessage)) {
                e.preventDefault();
            }
        });
    });


    // Highlight row khi chọn checkbox
    const orderCheckboxesHighlight = document.querySelectorAll(".order-checkbox");

    orderCheckboxesHighlight.forEach(cb => {
        cb.addEventListener("change", function () {
            const row = cb.closest("tr");

            if (cb.checked) {
                row.classList.add("selected-row");
            } else {
                row.classList.remove("selected-row");
            }
        });
    });

});