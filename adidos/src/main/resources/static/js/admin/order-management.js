document.addEventListener("DOMContentLoaded", function () {
    const checkAll = document.getElementById("checkAll");

    if (checkAll) {
        checkAll.addEventListener("change", function () {
            document.querySelectorAll("input[name='orderIds']").forEach(cb => {
                cb.checked = checkAll.checked;
            });
        });
    }
});s