let addressLoaded = false;

async function loadProvinces() {
    const provinceSelect = document.getElementById("province");
    if (!provinceSelect) return;

    if (addressLoaded && provinceSelect.options.length > 1) return;

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

    addressLoaded = true;
}

async function loadDistricts(provinceCode) {
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    if (!districtSelect || !wardSelect || !provinceCode) return;

    districtSelect.innerHTML =
        '<option value="">Đang tải Quận / Huyện...</option>';

    wardSelect.innerHTML =
        '<option value="">-- Chọn Phường / Xã --</option>';

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
}

async function loadWards(districtCode) {
    const wardSelect = document.getElementById("ward");

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

async function openAddressModal() {
    const modal = document.getElementById('addressModal');
    const form = document.getElementById('addressForm');
    const title = document.getElementById('addressModalTitle');
    const defaultCheckbox = document.getElementById('isDefault');

    if (!modal || !form || !title) return;

    title.innerText = 'Thêm địa chỉ mới';
    form.action = '/profile/addresses/add';
    form.reset();

    await loadProvinces();

    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    if (districtSelect) {
        districtSelect.innerHTML =
            '<option value="">-- Chọn Quận / Huyện --</option>';
    }

    if (wardSelect) {
        wardSelect.innerHTML =
            '<option value="">-- Chọn Phường / Xã --</option>';
    }

    if (defaultCheckbox) {
        defaultCheckbox.checked = false;
    }

    modal.classList.add('show');
}

async function openEditAddressModal(id, receiverName, phone, province, district, ward, addressDetail, isDefault) {
    const modal = document.getElementById('addressModal');
    const form = document.getElementById('addressForm');
    const title = document.getElementById('addressModalTitle');

    if (!modal || !form || !title) return;

    title.innerText = 'Cập nhật địa chỉ';
    form.action = '/profile/addresses/update/' + id;

    document.getElementById('receiverName').value = receiverName || '';
    document.getElementById('addressPhone').value = phone || '';
    document.getElementById('addressDetail').value = addressDetail || '';

    const defaultCheckbox = document.getElementById('isDefault');
    if (defaultCheckbox) {
        defaultCheckbox.checked = isDefault === 'true';
    }

    await loadProvinces();

    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    provinceSelect.value = province || '';

    const selectedProvince = provinceSelect.options[provinceSelect.selectedIndex];
    const provinceCode = selectedProvince?.getAttribute("data-code");

    if (provinceCode) {
        await loadDistricts(provinceCode);
        districtSelect.value = district || '';
    }

    const selectedDistrict = districtSelect.options[districtSelect.selectedIndex];
    const districtCode = selectedDistrict?.getAttribute("data-code");

    if (districtCode) {
        await loadWards(districtCode);
        wardSelect.value = ward || '';
    }

    modal.classList.add('show');
}

function closeAddressModal() {
    const modal = document.getElementById('addressModal');

    if (modal) {
        modal.classList.remove('show');
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const confirmActions = document.querySelectorAll('.confirm-action');

    confirmActions.forEach(element => {
        element.addEventListener('submit', function (e) {
            const message = this.getAttribute('data-message') || 'Bạn có chắc chắn muốn thực hiện?';

            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    const avatarFile = document.getElementById('avatarFile');
    const avatarPreview = document.getElementById('avatarPreview');

    if (avatarFile && avatarPreview) {
        avatarFile.addEventListener('change', function (event) {
            const file = event.target.files[0];

            if (file) {
                const reader = new FileReader();

                reader.onload = function (e) {
                    avatarPreview.src = e.target.result;
                };

                reader.readAsDataURL(file);
            }
        });
    }

    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    if (provinceSelect && districtSelect && wardSelect) {
        loadProvinces();

        provinceSelect.addEventListener("change", async function () {
            const selectedOption = this.options[this.selectedIndex];
            const provinceCode = selectedOption?.getAttribute("data-code");

            districtSelect.innerHTML =
                '<option value="">-- Chọn Quận / Huyện --</option>';

            wardSelect.innerHTML =
                '<option value="">-- Chọn Phường / Xã --</option>';

            if (provinceCode) {
                await loadDistricts(provinceCode);
            }
        });

        districtSelect.addEventListener("change", async function () {
            const selectedOption = this.options[this.selectedIndex];
            const districtCode = selectedOption?.getAttribute("data-code");

            wardSelect.innerHTML =
                '<option value="">-- Chọn Phường / Xã --</option>';

            if (districtCode) {
                await loadWards(districtCode);
            }
        });
    }
});