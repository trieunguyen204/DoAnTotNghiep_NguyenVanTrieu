/**
 * JS QUẢN LÝ VOUCHER - Tích hợp Validation Date
 */
document.addEventListener("DOMContentLoaded", function() {
    const tableBody = document.getElementById('voucher-table-body');
    const modal = document.getElementById('voucher-modal');
    const form = document.getElementById('voucher-form');

    // Tự động load danh sách khi vào trang
    loadVouchers();

    // Mở modal thêm mới
    document.getElementById('btn-add-voucher').addEventListener('click', () => {
        form.reset();
        document.getElementById('voucher-id').value = '';
        document.getElementById('modal-title').innerText = 'Tạo Voucher Mới';
        modal.classList.add('active');
    });

    // Đóng modal
    document.getElementById('close-voucher-modal').addEventListener('click', () => {
        modal.classList.remove('active');
    });

    // Hàm lấy danh sách Voucher
    function loadVouchers() {
        fetch('/api/admin/vouchers')
            .then(res => res.json())
            .then(data => {
                tableBody.innerHTML = '';
                data.forEach(v => {
                    const formatMoney = (val) => val ? new Intl.NumberFormat('vi-VN').format(val) + 'đ' : '-';
                    tableBody.innerHTML += `
                        <tr>
                            <td><span style="font-family: monospace; font-weight: 700; background: #f1f5f9; padding: 4px 8px; border-radius: 4px;">${v.code}</span></td>
                            <td>${v.discountType === 'PERCENT' ? 'Giảm %' : 'Tiền mặt'}</td>
                            <td><strong>${new Intl.NumberFormat('vi-VN').format(v.discountValue)}${v.discountType === 'PERCENT' ? '%' : 'đ'}</strong></td>
                            <td>${formatMoney(v.minOrderValue)}</td>
                            <td>${v.discountType === 'PERCENT' ? formatMoney(v.maxDiscountValue) : '<span style="color:#cbd5e1">-</span>'}</td>
                            <td>${v.usageLimit}</td>
                            <td>${v.usedCount || 0}</td>
                            <td style="font-size: 11px; color: #64748b">
                                <div><i class="far fa-clock"></i> ${new Date(v.startDate).toLocaleString('vi-VN')}</div>
                                <div><i class="fas fa-history"></i> ${new Date(v.endDate).toLocaleString('vi-VN')}</div>
                            </td>
                            <td><span class="badge ${v.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
                                ${v.status === 'ACTIVE' ? 'Đang chạy' : 'Tạm dừng'}</span>
                            </td>
                            <td class="action-buttons">
                                <button class="btn-edit" onclick="editVoucher(${v.id})" title="Sửa"><i class="fas fa-pen"></i></button>
                                <button class="btn-delete" onclick="deleteVoucher(${v.id})" title="Xóa"><i class="fas fa-trash"></i></button>
                            </td>
                        </tr>
                    `;
                });
            });
    }

    // Xử lý gửi Form (Thêm/Sửa) + Bắt lỗi ngày tháng
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        // 1. Lấy dữ liệu
        const id = document.getElementById('voucher-id').value;
        const startDateVal = document.getElementById('start-date').value;
        const endDateVal = document.getElementById('end-date').value;

        // 2. LOGIC BẮT LỖI NGÀY THÁNG
        const startDate = new Date(startDateVal);
        const endDate = new Date(endDateVal);

        if (endDate <= startDate) {
            alert('❌ LỖI: Ngày kết thúc mã giảm giá phải diễn ra SAU ngày bắt đầu. Vui lòng kiểm tra lại!');
            document.getElementById('end-date').focus();
            return; // Chặn không cho gửi request
        }

        // 3. Chuẩn bị Object gửi đi
        const data = {
            code: document.getElementById('voucher-code').value.toUpperCase(),
            discountType: document.getElementById('discount-type').value,
            discountValue: parseFloat(document.getElementById('discount-value').value),
            minOrderValue: parseFloat(document.getElementById('min-order-value').value),
            maxDiscountValue: document.getElementById('max-discount-value').value ? parseFloat(document.getElementById('max-discount-value').value) : null,
            usageLimit: parseInt(document.getElementById('voucher-limit').value),
            startDate: startDateVal,
            endDate: endDateVal,
            status: document.getElementById('voucher-status').value
        };

        // 4. Gọi API
        fetch(id ? `/api/admin/vouchers/${id}` : '/api/admin/vouchers', {
            method: id ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(res => {
            if(res.ok) {
                modal.classList.remove('active');
                loadVouchers();
                alert('Thành công!');
            } else {
                alert('Có lỗi xảy ra từ máy chủ!');
            }
        });
    });

    // Hàm Edit
    window.editVoucher = function(id) {
        fetch(`/api/admin/vouchers/${id}`).then(res => res.json()).then(data => {
            document.getElementById('voucher-id').value = data.id;
            document.getElementById('voucher-code').value = data.code;
            document.getElementById('discount-type').value = data.discountType;
            document.getElementById('discount-value').value = data.discountValue;
            document.getElementById('min-order-value').value = data.minOrderValue;
            document.getElementById('max-discount-value').value = data.maxDiscountValue || '';
            document.getElementById('voucher-limit').value = data.usageLimit;
            // Cắt chuỗi lấy định dạng YYYY-MM-DDThh:mm cho datetime-local
            document.getElementById('start-date').value = data.startDate ? data.startDate.substring(0, 16) : '';
            document.getElementById('end-date').value = data.endDate ? data.endDate.substring(0, 16) : '';
            document.getElementById('voucher-status').value = data.status;

            document.getElementById('modal-title').innerText = 'Cập Nhật Voucher';
            modal.classList.add('active');
        });
    };

    // Hàm Xóa
    window.deleteVoucher = function(id) {
        if(confirm('Bạn có chắc chắn muốn xóa mã giảm giá này vĩnh viễn?')) {
            fetch(`/api/admin/vouchers/${id}`, { method: 'DELETE' })
                .then(() => loadVouchers());
        }
    };
});