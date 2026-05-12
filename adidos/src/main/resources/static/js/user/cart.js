document.addEventListener("DOMContentLoaded", function () {
    const formatMoney = value =>
        new Intl.NumberFormat("vi-VN").format(value) + "đ";

    function updateRow(row) {
        const input = row.querySelector(".qty-input");
        const minusBtn = row.querySelector(".btn-minus");
        const plusBtn = row.querySelector(".btn-plus");
        const subtotalEl = row.querySelector(".item-subtotal");

        if (!input) return;

        const price = Number(row.dataset.price || 0);
        const max = Number(input.dataset.max);
        let qty = Number(input.value);

        if (!qty || qty < 1) qty = 1;
        if (qty > max) qty = max;

        input.value = qty;

        if (minusBtn) minusBtn.disabled = qty <= 1;
        if (plusBtn) plusBtn.disabled = qty >= max;

        if (subtotalEl) {
            subtotalEl.textContent = formatMoney(price * qty);
        }
    }

    function updateCartTotal() {
        let total = 0;

        document.querySelectorAll(".cart-row").forEach(row => {
            const price = Number(row.dataset.price || 0);
            const input = row.querySelector(".qty-input");
            const qty = Number(input?.value) || 0;

            total += price * qty;
        });

        const totalEl = document.getElementById("cartSubtotal");
        if (totalEl) {
            totalEl.textContent = formatMoney(total);
        }
    }

    function syncQuantity(id, quantity) {
        return fetch(`/api/cart/update/${id}?quantity=${quantity}`, {
            method: "PUT",
            credentials: "same-origin"
        }).then(async response => {
            if (!response.ok) {
                const msg = await response.text();
                throw new Error(msg || "Không thể cập nhật số lượng");
            }
        });
    }

    document.querySelectorAll(".cart-row").forEach(row => {
        const input = row.querySelector(".qty-input");
        const minusBtn = row.querySelector(".btn-minus");
        const plusBtn = row.querySelector(".btn-plus");

        updateRow(row);

        minusBtn?.addEventListener("click", async function () {
            let qty = Number(input.value) || 1;
            qty = Math.max(1, qty - 1);

            input.value = qty;
            updateRow(row);
            updateCartTotal();

            try {
                await syncQuantity(input.dataset.id, qty);
                if (window.updateCartBadge) window.updateCartBadge();
            } catch (e) {
                alert(e.message);
                location.reload();
            }
        });

        plusBtn?.addEventListener("click", async function () {
            const max = Number(input.dataset.max);
            let qty = Number(input.value) || 1;

            if (qty >= max) {
                input.value = max;
                updateRow(row);
                updateCartTotal();
                return;
            }

            qty += 1;

            input.value = qty;
            updateRow(row);
            updateCartTotal();

            try {
                await syncQuantity(input.dataset.id, qty);
                if (window.updateCartBadge) window.updateCartBadge();
            } catch (e) {
                alert(e.message);
                location.reload();
            }
        });

        input?.addEventListener("keydown", function (e) {
            if (["e", "E", "+", "-", ".", ","].includes(e.key)) {
                e.preventDefault();
            }
        });

        input?.addEventListener("input", function () {
            this.value = this.value.replace(/[^\d]/g, "");

            const max = Number(this.dataset.max);
            let qty = Number(this.value);

            if (qty > max) this.value = max;
            if (qty < 1 && this.value !== "") this.value = 1;

            updateRow(row);
            updateCartTotal();
        });

        input?.addEventListener("blur", async function () {
            if (this.value === "" || Number(this.value) < 1) {
                this.value = 1;
            }

            updateRow(row);
            updateCartTotal();

            try {
                await syncQuantity(this.dataset.id, this.value);
                if (window.updateCartBadge) window.updateCartBadge();
            } catch (e) {
                alert(e.message);
                location.reload();
            }
        });
    });

    document.querySelectorAll(".btn-remove-item").forEach(btn => {
        btn.addEventListener("click", function () {
            if (!confirm("Bạn có chắc muốn xóa sản phẩm này?")) return;

            const itemId = this.dataset.id;

            fetch(`/api/cart/remove/${itemId}`, {
                method: "DELETE",
                credentials: "same-origin"
            })
                .then(async response => {
                    if (!response.ok) {
                        const msg = await response.text();
                        throw new Error(msg || "Lỗi khi xóa sản phẩm");
                    }

                    const row = document.getElementById(`cart-item-${itemId}`);
                    row?.remove();

                    updateCartTotal();

                    if (window.updateCartBadge) window.updateCartBadge();

                    if (document.querySelectorAll(".cart-row").length === 0) {
                        location.reload();
                    }
                })
                .catch(error => {
                    alert(error.message);
                });
        });
    });

    updateCartTotal();
});