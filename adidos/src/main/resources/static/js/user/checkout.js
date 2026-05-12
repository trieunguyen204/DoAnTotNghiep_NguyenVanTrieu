document.addEventListener("DOMContentLoaded", function () {
    initVoucher();
    initGuestAddressSelect();
});

function initVoucher() {
    const voucherInput = document.getElementById("voucherCode");
    const voucherSearch = voucherInput;
    const btnApplyVoucher = document.getElementById("btnApplyVoucher");
    const btnCancelVoucher = document.getElementById("btnCancelVoucher");
    const voucherItems = document.querySelectorAll(".voucher-item");
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

    voucherSearch.addEventListener("input", function () {
        const keyword = this.value.toLowerCase().trim();

        voucherItems.forEach(item => {
            const code = item.getAttribute("data-code").toLowerCase();
            item.style.display = code.includes(keyword) ? "flex" : "none";
        });
    });

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

                    if (btnCancelVoucher) {
                        btnCancelVoucher.style.display = "inline-block";
                    }
                } else {
                    voucherMessage.className = "voucher-message error";
                    discountAmountText.innerText = "0đ";
                    finalAmountText.innerText = formatMoney(totalPrice + shippingFee);

                    if (btnCancelVoucher) {
                        btnCancelVoucher.style.display = "none";
                    }
                }
            })
            .catch(() => {
                voucherMessage.innerText = "Không thể kiểm tra voucher";
                voucherMessage.className = "voucher-message error";
            });
    });

    if (btnCancelVoucher) {
        btnCancelVoucher.addEventListener("click", function () {
            const totalPrice = Number(totalPriceValue.value);
            const shippingFee = Number(shippingFeeValue.value);

            voucherInput.value = "";

            voucherItems.forEach(item => {
                item.classList.remove("selected");
                item.style.display = "flex";
            });

            voucherMessage.innerText = "Đã hủy sử dụng mã giảm giá";
            voucherMessage.className = "voucher-message";

            discountAmountText.innerText = "0đ";
            finalAmountText.innerText = formatMoney(totalPrice + shippingFee);

            btnCancelVoucher.style.display = "none";
        });
    }
}

function initGuestAddressSelect() {
    const provinceSelect = document.getElementById("guestProvince");
    const districtSelect = document.getElementById("guestDistrict");
    const wardSelect = document.getElementById("guestWard");

    if (!provinceSelect || !districtSelect || !wardSelect) return;

    loadGuestProvinces();

    provinceSelect.addEventListener("change", async function () {
        const selectedOption = this.options[this.selectedIndex];
        const provinceCode = selectedOption?.getAttribute("data-code");

        districtSelect.innerHTML =
            '<option value="">-- Chọn Quận / Huyện --</option>';

        wardSelect.innerHTML =
            '<option value="">-- Chọn Phường / Xã --</option>';

        if (provinceCode) {
            await loadGuestDistricts(provinceCode);
        }
    });

    districtSelect.addEventListener("change", async function () {
        const selectedOption = this.options[this.selectedIndex];
        const districtCode = selectedOption?.getAttribute("data-code");

        wardSelect.innerHTML =
            '<option value="">-- Chọn Phường / Xã --</option>';

        if (districtCode) {
            await loadGuestWards(districtCode);
        }
    });
}

async function loadGuestProvinces() {
    const provinceSelect = document.getElementById("guestProvince");
    if (!provinceSelect) return;

    provinceSelect.innerHTML =
        '<option value="">Đang tải Tỉnh / Thành phố...</option>';

    const response = await fetch("https://provinces.open-api.vn/api/p/");
    const provinces = await response.json();

    provinceSelect.innerHTML =
        '<option value="">-- Chọn Tỉnh / Thành phố --</option>';

    provinces.forEach(province => {
        provinceSelect.innerHTML += `
            <option value="${province.name}" data-code="${province.code}">
                ${province.name}
            </option>
        `;
    });
}

async function loadGuestDistricts(provinceCode) {
    const districtSelect = document.getElementById("guestDistrict");
    const wardSelect = document.getElementById("guestWard");

    if (!districtSelect || !wardSelect || !provinceCode) return;

    districtSelect.innerHTML =
        '<option value="">Đang tải Quận / Huyện...</option>';

    const response = await fetch(
        `https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`
    );

    const data = await response.json();

    districtSelect.innerHTML =
        '<option value="">-- Chọn Quận / Huyện --</option>';

    data.districts.forEach(district => {
        districtSelect.innerHTML += `
            <option value="${district.name}" data-code="${district.code}">
                ${district.name}
            </option>
        `;
    });

    wardSelect.innerHTML =
        '<option value="">-- Chọn Phường / Xã --</option>';
}

async function loadGuestWards(districtCode) {
    const wardSelect = document.getElementById("guestWard");

    if (!wardSelect || !districtCode) return;

    wardSelect.innerHTML =
        '<option value="">Đang tải Phường / Xã...</option>';

    const response = await fetch(
        `https://provinces.open-api.vn/api/d/${districtCode}?depth=2`
    );

    const data = await response.json();

    wardSelect.innerHTML =
        '<option value="">-- Chọn Phường / Xã --</option>';

    data.wards.forEach(ward => {
        wardSelect.innerHTML += `
            <option value="${ward.name}">
                ${ward.name}
            </option>
        `;
    });
}

function formatMoney(value) {
    return new Intl.NumberFormat("vi-VN").format(value) + "đ";
}