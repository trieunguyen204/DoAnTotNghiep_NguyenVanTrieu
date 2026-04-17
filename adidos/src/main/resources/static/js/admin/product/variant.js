document.addEventListener('DOMContentLoaded', function() {
    // --- XỬ LÝ MODAL THÊM BIẾN THỂ ---
    const variantModal = document.getElementById('variantModal');
    const variantForm = variantModal.querySelector('form');
    const variantModalTitle = variantModal.querySelector('.modal-header h2');

    document.getElementById('btnAddVariant').addEventListener('click', () => {

        variantForm.reset(); // Xóa dữ liệu cũ
        document.getElementById('varId').value = ''; // Reset ID
        variantModalTitle.textContent = 'Thêm Biến Thể Mới';
        variantModal.style.display = 'flex';
    });

    // Nút mở form Sửa
    document.querySelectorAll('.edit-variant-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            // Đổ dữ liệu từ data-* vào form
            document.getElementById('varId').value = this.dataset.id;
            variantForm.querySelector('select[name="colorId"]').value = this.dataset.colorId;
            variantForm.querySelector('select[name="sizeId"]').value = this.dataset.sizeId;
            variantForm.querySelector('input[name="price"]').value = this.dataset.price;
            variantForm.querySelector('input[name="stockQuantity"]').value = this.dataset.stock;

            variantModalTitle.textContent = 'Cập Nhật Biến Thể';
            variantModal.style.display = 'flex';
        });
    });

    // --- XỬ LÝ MODAL HÌNH ẢNH ---
    const imageModal = document.getElementById('imageModal');
    const uploadForm = document.getElementById('uploadImageForm');
    const gallery = document.getElementById('imageGallery');

    document.querySelectorAll('.btn-manage-images').forEach(btn => {
        btn.addEventListener('click', function() {
            const variantId = this.dataset.id;

            // Cập nhật action cho form upload
            uploadForm.action = `/admin/products/${productId}/variants/${variantId}/images/upload`;

            // Gọi API lấy danh sách ảnh
            fetch(`/admin/products/${productId}/variants/${variantId}/images`)
                .then(res => res.json())
                .then(data => {
                    document.getElementById('imgModalVariantName').textContent = `${data.colorName} - ${data.sizeName}`;

                    // Render UI
                    gallery.innerHTML = '';
                    if (!data.imageUrlsWithData || data.imageUrlsWithData.length === 0) {
                        gallery.innerHTML = '<p class="text-muted">Chưa có hình ảnh nào.</p>';
                    } else {
                        data.imageUrlsWithData.forEach(img => {
                            const isPrimaryClass = img.isPrimary ? 'primary-img' : '';
                            const primaryBadge = img.isPrimary ? '<span class="badge-primary-label">Ảnh chính</span>' : '';

                            gallery.innerHTML += `
                                <div class="img-card ${isPrimaryClass}">
                                    <img src="${img.url}" alt="Product Image">
                                    ${primaryBadge}
                                    <div class="img-actions">
                                        ${!img.isPrimary ? `
                                        <form action="/admin/products/${productId}/variants/${variantId}/images/${img.id}/primary" method="post" class="d-inline">
                                            <button type="submit" class="btn-action btn-info btn-sm" title="Đặt làm ảnh chính"><i class="fas fa-star"></i></button>
                                        </form>` : ''}

                                        <form action="/admin/products/${productId}/variants/${variantId}/images/${img.id}/delete" method="post" class="d-inline form-delete-img">
                                            <button type="submit" class="btn-action btn-danger btn-sm" title="Xóa ảnh"><i class="fas fa-trash"></i></button>
                                        </form>
                                    </div>
                                </div>
                            `;
                        });
                    }
                    imageModal.style.display = 'flex';
                });
        });
    });

    // Đóng Modal chung
    document.querySelectorAll('.close-modal, .close-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            variantModal.style.display = 'none';
            imageModal.style.display = 'none';
        });
    });

    // Xác nhận xóa
    document.body.addEventListener('submit', function(e) {
        if (e.target.classList.contains('form-delete') || e.target.classList.contains('form-delete-img')) {
            if (!confirm('Bạn có chắc chắn muốn xóa? Hành động này không thể hoàn tác!')) {
                e.preventDefault();
            }
        }
    });
});