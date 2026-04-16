document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('categoryModal');
    const form = document.getElementById('categoryForm');
    const modalTitle = document.getElementById('modalTitle');

    // Mở modal Thêm
    document.getElementById('btnAddCategory').addEventListener('click', () => {
        form.reset();
        document.getElementById('catId').value = '';
        document.getElementById('catParent').value = '';
        modalTitle.textContent = 'Thêm Danh Mục Mới';
        modal.style.display = 'flex';
    });

    // Mở modal Sửa
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.getElementById('catId').value = this.dataset.id;
            document.getElementById('catName').value = this.dataset.name;
            document.getElementById('catParent').value = this.dataset.parent || '';

            modalTitle.textContent = 'Cập Nhật Danh Mục';
            modal.style.display = 'flex';
        });
    });

    // Đóng Modal
    document.querySelectorAll('.close-modal, .close-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => modal.style.display = 'none');
    });

    // Nhấn ra ngoài modal để đóng
    window.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
    });

    // Xác nhận xóa
    document.querySelectorAll('.form-delete').forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Cảnh báo: Xóa danh mục này có thể gặp lỗi nếu đang có danh mục con hoặc sản phẩm bên trong. Bạn có chắc chắn muốn xóa?')) {
                e.preventDefault();
            }
        });
    });
});

// Tự động submit khi xóa hết chữ trong ô search (tiện cho người dùng)
document.querySelectorAll('.search-input').forEach(input => {
    input.addEventListener('input', function() {
        if (this.value === '') {
            this.closest('form').submit();
        }
    });
});