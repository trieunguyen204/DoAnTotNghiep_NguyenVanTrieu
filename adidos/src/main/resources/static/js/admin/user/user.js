document.addEventListener('DOMContentLoaded', function() {
    
    // 1. Xác nhận Khóa / Mở khóa
    const statusForms = document.querySelectorAll('.form-status');
    statusForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const isLocking = form.querySelector('input[name="status"]').value === 'LOCKED';
            if (!confirm(`Bạn có chắc chắn muốn ${isLocking ? 'KHÓA' : 'MỞ KHÓA'} tài khoản này?`)) {
                e.preventDefault();
            }
        });
    });

    // 2. Xác nhận Xóa
    document.querySelectorAll('.form-delete').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const actionUrl = this.getAttribute('action').replace('/delete', '');

            if (confirm('CẢNH BÁO: Bạn có chắc chắn muốn XÓA VĨNH VIỄN tài khoản này?')) {
                fetch(actionUrl, {
                    method: 'DELETE',
                    headers: {
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content
                    }
                }).then(response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert("Lỗi khi xóa người dùng.");
                    }
                });
            }
        });
    });

    // 3. Logic Edit Modal
    const modal = document.getElementById('editUserModal');
    const editForm = document.getElementById('editForm');

    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const userId = this.dataset.id;
            const fullName = this.dataset.fullname;
            const phone = this.dataset.phone || '';
            const role = this.dataset.role;

            editForm.action = `/admin/users/${userId}/edit`;

            document.getElementById('editFullName').value = fullName;
            document.getElementById('editPhone').value = phone;
            document.getElementById('editRole').value = role;

            modal.style.display = 'flex';
        });
    });

    // KHOẢNG THIẾU: Cần khai báo closeBtns ở đây
    const closeBtns = document.querySelectorAll('.close-modal, .close-modal-btn');

    // Đóng modal
    closeBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            modal.style.display = 'none';
        });
    });

    // Nhấn ngoài vùng modal để đóng
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
});