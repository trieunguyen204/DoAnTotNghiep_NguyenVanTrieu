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

                // CÁCH LẤY ID CHUẨN 100%: Cắt trực tiếp từ URL hiện tại (/admin/products/1/variants)
                const currentProductId = window.location.pathname.split('/')[3];

                // Cập nhật action cho form upload
                uploadForm.action = `/admin/products/${currentProductId}/variants/${variantId}/images/upload`;

                // Gọi API lấy danh sách ảnh
                fetch(`/admin/products/${currentProductId}/variants/${variantId}/images`)
                    .then(res => res.json())
                    .then(data => {
                        document.getElementById('imgModalVariantName').textContent = `${data.colorName} - ${data.sizeName}`;

                        // Render UI
                        gallery.innerHTML = '';
                        if (!data.imageUrlsWithData || data.imageUrlsWithData.length === 0) {
                            gallery.innerHTML = '<p class="text-muted">Chưa có hình ảnh nào.</p>';
                        } else {
                            data.imageUrlsWithData.forEach(img => {
                                // Đảm bảo đường dẫn có tiền tố /uploads/
                                const finalUrl = img.url.startsWith('http') || img.url.startsWith('/') ? img.url : '/uploads/' + img.url;
                                const isPrimary = img.isPrimary === true;

                                // DÙNG THẺ FORM ĐỂ GỌI API ĐẶT ẢNH CHÍNH VÀ XÓA THAY VÌ ONCLICK
                                gallery.innerHTML += `
                                    <div class="img-card ${isPrimary ? 'primary-img' : ''}" style="border: ${isPrimary ? '2px solid #28a745' : '1px solid #ddd'}; padding: 5px; margin: 5px; display: inline-block; position: relative;">
                                        <img src="${finalUrl}" alt="img" style="width:100px; height:100px; object-fit:cover;">
                                        ${isPrimary ? '<span class="badge-primary" style="position: absolute; top:0; left:0; background: green; color: white; font-size: 10px; padding: 2px;">Ảnh chính</span>' : ''}

                                        <div class="img-actions" style="margin-top: 5px; text-align: center;">
                                            ${!isPrimary ? `
                                            <form action="/admin/products/${currentProductId}/variants/${variantId}/images/${img.id}/primary" method="post" class="d-inline">
                                                <button type="submit" class="btn-action btn-info btn-sm" title="Đặt làm ảnh chính"><i class="fas fa-star"></i></button>
                                            </form>` : ''}

                                            <form action="/admin/products/${currentProductId}/variants/${variantId}/images/${img.id}/delete" method="post" class="d-inline form-delete-img">
                                                <button type="submit" class="btn-action btn-danger btn-sm" title="Xóa ảnh"><i class="fas fa-trash"></i></button>
                                            </form>
                                        </div>
                                    </div>`;
                            });
                        }
                        imageModal.style.display = 'flex';
                    })
                    .catch(err => {
                        console.error("Lỗi khi tải ảnh:", err);
                        gallery.innerHTML = '<p class="text-danger">Không thể tải hình ảnh.</p>';
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