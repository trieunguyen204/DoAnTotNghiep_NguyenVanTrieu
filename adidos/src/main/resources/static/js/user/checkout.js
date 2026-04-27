document.addEventListener("DOMContentLoaded", function () {
    const voucherItems = document.querySelectorAll(".voucher-item");
    const voucherSearch = document.getElementById("voucherSearch");
    const voucherInput = document.getElementById("voucherCode");
    const btnApplyVoucher = document.getElementById("btnApplyVoucher");
    const voucherMessage = document.getElementById("voucherMessage");
    const discountAmountText = document.getElementById("discountAmountText");
    const finalAmountText = document.getElementById("finalAmountText");
    const totalPriceValue = document.getElementById("totalPriceValue");
    const shippingFeeValue = document.getElementById("shippingFeeValue");

    if (!btnApplyVoucher || !voucherInput) return;

    voucherItems.forEach(item => {
        const selectBtn = item.querySelector(".btn-select-voucher");

        if (!selectBtn) return;

        selectBtn.addEventListener("click", function () {
            const code = item.getAttribute("data-code");

            voucherInput.value = code;

            voucherItems.forEach(v => v.classList.remove("selected"));
            item.classList.add("selected");

            btnApplyVoucher.click();
        });
    });

    if (voucherSearch) {
        voucherSearch.addEventListener("input", function () {
            const keyword = this.value.toLowerCase().trim();

            voucherItems.forEach(item => {
                const code = item.getAttribute("data-code").toLowerCase();
                item.style.display = code.includes(keyword) ? "flex" : "none";
            });
        });
    }

    btnApplyVoucher.addEventListener("click", function () {
        const totalPrice = Number(totalPriceValue.value);
        const shippingFee = Number(shippingFeeValue.value);
        const code = voucherInput.value.trim();

        if (!code) {
            voucherMessage.innerText = "Vui lòng nhập mã giảm giá";
            voucherMessage.className = "voucher-message error";
            return;
        }

        fetch(`/api/vouchers/check?code=${encodeURIComponent(code)}&total=${totalPrice}`)
            .then(res => res.json())
            .then(data => {
                const discount = Number(data.discountAmount || 0);
                const finalAmount = totalPrice + shippingFee - discount;

                voucherMessage.innerText = data.message;

                if (data.success) {
                    voucherMessage.className = "voucher-message success";
                    discountAmountText.innerText = formatMoney(discount);
                    finalAmountText.innerText = formatMoney(finalAmount);
                } else {
                    voucherMessage.className = "voucher-message error";
                    discountAmountText.innerText = "0đ";
                    finalAmountText.innerText = formatMoney(totalPrice + shippingFee);
                }
            })
            .catch(() => {
                voucherMessage.innerText = "Không thể kiểm tra voucher";
                voucherMessage.className = "voucher-message error";
            });
    });
});

function formatMoney(value) {
    return new Intl.NumberFormat("vi-VN").format(value) + "đ";
}