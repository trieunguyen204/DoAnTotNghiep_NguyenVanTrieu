document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('productModal');
    const form = document.getElementById('productForm');
    const closeBtns = document.querySelectorAll('.close-modal, .close-modal-btn');
    const modalTitle = document.getElementById('modalTitle');

    // Mở modal Thêm
    document.getElementById('btnAddProduct').addEventListener('click', () => {
        form.reset();
        document.getElementById('prodId').value = '';
        $('#productCategory').val(null).trigger('change');

        document.getElementById('prodStatus').value = 'ACTIVE';

        modalTitle.textContent = 'Thêm Sản Phẩm Mới';
        modal.style.display = 'flex';
    });

    // Mở modal Sửa
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.getElementById('prodId').value = this.dataset.id || '';
            document.getElementById('prodName').value = this.dataset.name || '';
            document.getElementById('prodBrand').value = this.dataset.brand || '';
            document.getElementById('prodMaterial').value = this.dataset.material || '';
            document.getElementById('prodDesc').value = this.dataset.description || '';

            if(this.dataset.gender) {
                document.getElementById('prodGender').value = this.dataset.gender;
            }

            document.getElementById('prodStatus').value = this.dataset.status || 'ACTIVE';

            // ĐỔ DỮ LIỆU VÀ UPDATE UI CHO SELECT2 (Sửa lại ID cho đúng)
            const categoryId = this.getAttribute('data-category');

            if (categoryId && $('#productCategory option[value="' + categoryId + '"]').length > 0) {
                $('#productCategory').val(categoryId).trigger('change');
            } else {
                $('#productCategory').val('').trigger('change');
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


let searchTimeout;


document.querySelectorAll('input.search-input').forEach(input => {
    input.addEventListener('input', function(e) {

        clearTimeout(searchTimeout);


        searchTimeout = setTimeout(() => {

            if (this.value === '') {
                this.closest('form').submit();
            }
        }, 300);
    });
});
// Khởi tạo Select2
$(document).ready(function() {
    $('.select-search').select2({
        placeholder: "-- Tìm kiếm danh mục --",
        allowClear: true,
        width: '100%',
        dropdownParent: $('#productModal')
    });
});