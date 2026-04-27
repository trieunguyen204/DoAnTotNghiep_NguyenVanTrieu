function openAddressModal() {
    const modal = document.getElementById('addressModal');
    const form = document.getElementById('addressForm');
    const title = document.getElementById('addressModalTitle');
    const defaultCheckbox = document.getElementById('isDefault');

    if (!modal || !form || !title) return;

    title.innerText = 'Thêm địa chỉ mới';
    form.action = '/profile/addresses/add';
    form.reset();

    if (defaultCheckbox) {
        defaultCheckbox.checked = false;
    }

    modal.classList.add('show');
}

function openEditAddressModal(id, receiverName, phone, province, district, ward, addressDetail, isDefault) {
    const modal = document.getElementById('addressModal');
    const form = document.getElementById('addressForm');
    const title = document.getElementById('addressModalTitle');

    if (!modal || !form || !title) return;

    title.innerText = 'Cập nhật địa chỉ';
    form.action = '/profile/addresses/update/' + id;

    document.getElementById('receiverName').value = receiverName || '';
    document.getElementById('addressPhone').value = phone || '';
    document.getElementById('province').value = province || '';
    document.getElementById('district').value = district || '';
    document.getElementById('ward').value = ward || '';
    document.getElementById('addressDetail').value = addressDetail || '';

    const defaultCheckbox = document.getElementById('isDefault');
    if (defaultCheckbox) {
        defaultCheckbox.checked = isDefault === 'true';
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
});