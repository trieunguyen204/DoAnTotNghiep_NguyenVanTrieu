document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("variantModal");
    const modalBody = document.getElementById("variantModalBody");
    const closeBtn = document.getElementById("closeVariantModal");
    const buttons = document.querySelectorAll(".btn-view-variants");

    buttons.forEach(btn => {
        btn.addEventListener("click", function () {
            const productId = btn.getAttribute("data-product-id");

            modal.classList.add("show");
            modalBody.innerHTML = "Đang tải...";

            fetch(`/admin/product-analytics/${productId}/variants`)
                .then(res => res.json())
                .then(data => {
                    if (!data || data.length === 0) {
                        modalBody.innerHTML = `<div class="empty-box">Không có dữ liệu biến thể.</div>`;
                        return;
                    }

                    let html = `
                        <table class="variant-table">
                            <thead>
                                <tr>
                                    <th>Màu</th>
                                    <th>Size</th>
                                    <th>Tồn kho</th>
                                    <th>Đã bán</th>
                                </tr>
                            </thead>
                            <tbody>
                    `;

                    data.forEach(item => {
                        html += `
                            <tr>
                                <td>${item.colorName}</td>
                                <td>${item.sizeName}</td>
                                <td>${item.stockQuantity}</td>
                                <td><strong>${item.soldQuantity}</strong></td>
                            </tr>
                        `;
                    });

                    html += `
                            </tbody>
                        </table>
                    `;

                    modalBody.innerHTML = html;
                })
                .catch(() => {
                    modalBody.innerHTML = `<div class="empty-box">Không thể tải dữ liệu.</div>`;
                });
        });
    });

    closeBtn.addEventListener("click", function () {
        modal.classList.remove("show");
    });

    modal.addEventListener("click", function (e) {
        if (e.target === modal) {
            modal.classList.remove("show");
        }
    });
});