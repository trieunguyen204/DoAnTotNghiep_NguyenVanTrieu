const modal = document.getElementById('attrModal');
const form = document.getElementById('attrForm');
const modalTitle = document.getElementById('modalTitle');
const inputName = document.getElementById('attrInputName');
const labelName = document.getElementById('labelName');

function openModal(type) {
    form.reset();
    document.getElementById('attrId').value = '';
    if (type === 'color') {
        modalTitle.textContent = 'Thêm màu sắc mới';
        labelName.textContent = 'Tên màu sắc';
        inputName.name = 'colorName';
        form.action = '/admin/attributes/colors/save';
    } else {
        modalTitle.textContent = 'Thêm kích cỡ mới';
        labelName.textContent = 'Tên kích cỡ';
        inputName.name = 'sizeName';
        form.action = '/admin/attributes/sizes/save';
    }
    modal.style.display = 'flex';
}

function editAttr(type, id, name) {
    document.getElementById('attrId').value = id;
    inputName.value = name;
    if (type === 'color') {
        modalTitle.textContent = 'Sửa màu sắc';
        labelName.textContent = 'Tên màu sắc';
        inputName.name = 'colorName';
        form.action = '/admin/attributes/colors/save';
    } else {
        modalTitle.textContent = 'Sửa kích cỡ';
        labelName.textContent = 'Tên kích cỡ';
        inputName.name = 'sizeName';
        form.action = '/admin/attributes/sizes/save';
    }
    modal.style.display = 'flex';
}

document.querySelectorAll('.close-modal, .close-modal-btn').forEach(btn => {
    btn.onclick = () => modal.style.display = 'none';
});

window.onclick = (e) => { if (e.target === modal) modal.style.display = 'none'; };

document.querySelectorAll('.form-delete').forEach(f => {
    f.onsubmit = (e) => { if (!confirm('Xóa cái này có thể ảnh hưởng đến sản phẩm hiện có. Chắc chứ?')) e.preventDefault(); };
});

// Tự động submit khi xóa hết chữ trong ô search (tiện cho người dùng)
document.querySelectorAll('.search-input').forEach(input => {
    input.addEventListener('input', function() {
        if (this.value === '') {
            this.closest('form').submit();
        }
    });
});