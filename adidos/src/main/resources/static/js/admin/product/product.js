document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('productModal');
    const form = document.getElementById('productForm');
    const closeBtns = document.querySelectorAll('.close-modal, .close-modal-btn');
    const modalTitle = document.getElementById('modalTitle');

    // Mở modal Thêm
    document.getElementById('btnAddProduct').addEventListener('click', () => {
        form.reset();
        document.getElementById('prodId').value = '';
        modalTitle.textContent = 'Thêm Sản Phẩm Mới';
        modal.style.display = 'flex';
    });

    // Mở modal Sửa
        document.querySelectorAll('.edit-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                // Tất cả gán giá trị phải nằm TRONG NÀY
                document.getElementById('prodId').value = this.dataset.id || '';
                document.getElementById('prodName').value = this.dataset.name || '';
                document.getElementById('prodBrand').value = this.dataset.brand || '';

                document.getElementById('prodMaterial').value = this.dataset.material || '';
                document.getElementById('prodDesc').value = this.dataset.description || '';

                if(this.dataset.gender) {
                    document.getElementById('prodGender').value = this.dataset.gender;
                }

                // Chuyển 2 dòng này vào đây:
                document.getElementById('prodStatus').value = this.dataset.status || 'ACTIVE';
                if(this.dataset.category) {
                    document.getElementById('prodCategory').value = this.dataset.category;
                }

                modalTitle.textContent = 'Cập Nhật Sản Phẩm';
                modal.style.display = 'flex';
            });
        });

    // Đóng Modal
    closeBtns.forEach(btn => {
        btn.addEventListener('click', () => modal.style.display = 'none');
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
    });

    // Xác nhận xóa
    document.querySelectorAll('.form-delete').forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này? Các biến thể liên quan cũng sẽ bị xóa!')) {
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