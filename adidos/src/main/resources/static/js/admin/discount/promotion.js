const API_URL = '/admin/promotions/api';

document.addEventListener('DOMContentLoaded', loadPromotions);

async function loadPromotions() {
    const res = await fetch(API_URL);
    const data = await res.json();
    const tbody = document.querySelector('#promotionTable tbody');

    tbody.innerHTML = data.map(p => `
        <tr>
            <td>#${p.id}</td>
            <td><strong>${p.promotionName}</strong></td>
            <td><span class="badge-info">${p.discountType === 'PERCENT' ? 'Phần trăm' : 'Cố định'}</span></td>
            <td class="text-danger font-bold">${p.discountValue.toLocaleString()}${p.discountType === 'PERCENT' ? '%' : 'đ'}</td>
            <td><small>${new Date(p.startDate).toLocaleDateString()} - ${new Date(p.endDate).toLocaleDateString()}</small></td>
            <td><span class="badge-${p.status === 'ACTIVE' ? 'success' : 'danger'}">${p.status}</span></td>
            <td class="text-center">
                <button class="btn-edit" onclick="editPromotion(${p.id})"><i class="fas fa-edit"></i></button>
                <button class="btn-delete" onclick="deletePromotion(${p.id})"><i class="fas fa-trash"></i></button>
            </td>
        </tr>
    `).join('');
}


// Bật bộ tìm kiếm khi load trang
$(document).ready(function() {
    loadPromotions();
    $('#categoryIds').select2({
        placeholder: "Tìm kiếm danh mục...",
        allowClear: true
    });
});

async function editPromotion(id) {
    const res = await fetch(API_URL);
    const list = await res.json();
    const p = list.find(item => item.id === id);

    if (p) {
        document.getElementById('id').value = p.id;
        document.getElementById('promotionName').value = p.promotionName;
        document.getElementById('discountType').value = p.discountType;
        document.getElementById('discountValue').value = p.discountValue;
        document.getElementById('maxDiscountValue').value = p.maxDiscountValue || '';
        document.getElementById('priority').value = p.priority;
        // Fix lỗi ngày tháng bằng cách kiểm tra và cắt chuỗi an toàn
        document.getElementById('startDate').value = p.startDate ? p.startDate.slice(0, 16) : '';
        document.getElementById('endDate').value = p.endDate ? p.endDate.slice(0, 16) : '';
        document.getElementById('status').value = p.status;


        if (p.categories && p.categories.length > 0) {
            const catIds = p.categories.map(c => c.id);
            $('#categoryIds').val(catIds).trigger('change');
        } else {
            $('#categoryIds').val(null).trigger('change');
        }

        document.getElementById('modalTitle').innerText = "Cập nhật khuyến mãi #" + id;
        openModal(true);
    }
}

async function savePromotion() {
    // Lấy dữ liệu từ bộ tìm kiếm Select2 thay vì DOM thuần
    const selectedCatIds = $('#categoryIds').val() || [];
    const selectedCategories = selectedCatIds.map(id => ({ id: parseInt(id) }));

    const id = document.getElementById('id').value;
    const payload = {
        id: id ? parseInt(id) : null,
        promotionName: document.getElementById('promotionName').value,
        discountType: document.getElementById('discountType').value,
        discountValue: parseFloat(document.getElementById('discountValue').value),
        maxDiscountValue: document.getElementById('maxDiscountValue').value ? parseFloat(document.getElementById('maxDiscountValue').value) : null,
        priority: parseInt(document.getElementById('priority').value),
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        status: document.getElementById('status').value,
        categories: selectedCategories // Bắn dữ liệu lên Backend
    };

    const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        closeModal();
        loadPromotions();
    }
}

function openModal(isEdit = false) {
    if (!isEdit) {
        document.getElementById('promotionForm').reset();
        document.getElementById('id').value = '';
        $('#categoryIds').val(null).trigger('change'); // Reset cả ô tìm kiếm
        document.getElementById('modalTitle').innerText = "Thêm khuyến mãi mới";
    }
    document.getElementById('promotionModal').classList.add('active');
    document.getElementById('modalOverlay').classList.add('active');
}

async function deletePromotion(id) {
    if (confirm('Xác nhận xóa chương trình này?')) {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        loadPromotions();
    }
}


function closeModal() {
    document.getElementById('promotionModal').classList.remove('active');
    document.getElementById('modalOverlay').classList.remove('active');
}

