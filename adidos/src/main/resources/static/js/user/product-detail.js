document.addEventListener("DOMContentLoaded", function() {
    // 1. Xử lý hiển thị duy nhất 1 khối cho mỗi màu sắc (Lọc theo color-item)
    const colorItems = document.querySelectorAll('.color-item');
    const uniqueColors = new Set();

    colorItems.forEach(item => {
        const colorName = item.getAttribute('data-color-name');
        if (uniqueColors.has(colorName)) {
            item.remove();
        } else {
            uniqueColors.add(colorName);
        }
    });

    const finalColorBtns = document.querySelectorAll('.color-btn');
    const sizeBtns = document.querySelectorAll('.size-btn');
    const btnShowMore = document.getElementById('btn-show-more');
    const priceDisplay = document.getElementById('product-price');
    const stockDisplay = document.querySelector('#product-stock span');

    // Hàm định dạng tiền tệ
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
    }

    finalColorBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            finalColorBtns.forEach(c => c.classList.remove('active'));
            this.classList.add('active');

            const selectedColor = this.getAttribute('data-color-name');

            // 2. Xử lý hiển thị Ảnh (Tối đa 4 ảnh đầu)
            const allWrappers = document.querySelectorAll('.img-wrapper');
            let colorImageCount = 0;

            allWrappers.forEach(wrapper => {
                if(wrapper.getAttribute('data-color-name') === selectedColor) {
                    colorImageCount++;
                    wrapper.style.display = (colorImageCount <= 4) ? 'block' : 'none';
                } else {
                    wrapper.style.display = 'none';
                }
            });

            if(colorImageCount > 4) {
                btnShowMore.style.display = 'block';
                btnShowMore.onclick = () => {
                    allWrappers.forEach(w => {
                        if(w.getAttribute('data-color-name') === selectedColor) w.style.display = 'block';
                    });
                    btnShowMore.style.display = 'none';
                };
            } else {
                btnShowMore.style.display = 'none';
            }

            // 3. Lọc kích cỡ theo màu và tự động chọn size đầu tiên
            let firstVisibleSize = null;
            sizeBtns.forEach(size => {
                size.classList.remove('active');
                if(size.getAttribute('data-color-name') === selectedColor) {
                    size.classList.remove('hidden');
                    if (!firstVisibleSize) firstVisibleSize = size;
                } else {
                    size.classList.add('hidden');
                }
            });

            // Tự động click size đầu tiên để cập nhật giá/số lượng
            if (firstVisibleSize) {
                firstVisibleSize.click();
            }
        });
    });

    // 4. Xử lý khi chọn size: Cập nhật giá và SỐ LƯỢNG
    sizeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            sizeBtns.forEach(s => s.classList.remove('active'));
            this.classList.add('active');

            // Cập nhật giá
            const price = this.getAttribute('data-price');
            if (price && priceDisplay) {
                priceDisplay.innerText = formatCurrency(price);
            }

            // CẬP NHẬT SỐ LƯỢNG TỒN KHO
            const stock = this.getAttribute('data-stock');
            if (stockDisplay) {
                stockDisplay.innerText = (stock > 0) ? stock : "Hết hàng";
            }
        });
    });

    // Tự động kích hoạt màu đầu tiên khi load trang
    if(finalColorBtns.length > 0) finalColorBtns[0].click();

    // ==========================================
    // Xử lý bật/tắt popup Hướng dẫn chọn size
    // ==========================================
    const openSizeGuideBtn = document.getElementById('open-size-guide');
    const closeSizeGuideBtn = document.getElementById('close-size-guide');
    const sizeModal = document.getElementById('size-modal');

    if (openSizeGuideBtn && closeSizeGuideBtn && sizeModal) {
        openSizeGuideBtn.addEventListener('click', () => {
            sizeModal.classList.add('show');
        });

        closeSizeGuideBtn.addEventListener('click', () => {
            sizeModal.classList.remove('show');
        });

        window.addEventListener('click', (e) => {
            if (e.target === sizeModal) {
                sizeModal.classList.remove('show');
            }
        });
    }
});