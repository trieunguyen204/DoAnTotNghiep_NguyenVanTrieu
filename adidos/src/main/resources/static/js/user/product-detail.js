document.addEventListener("DOMContentLoaded", function() {
    // 1. Xử lý hiển thị duy nhất 1 nút cho mỗi màu sắc
    const colorBtns = document.querySelectorAll('.color-btn');
    const uniqueColors = new Set();

    colorBtns.forEach(btn => {
        const colorName = btn.getAttribute('data-color-name');
        if (uniqueColors.has(colorName)) {
            btn.remove(); // Xóa bỏ nút màu trùng lặp
        } else {
            uniqueColors.add(colorName);
        }
    });

    const finalColorBtns = document.querySelectorAll('.color-btn');
    const sizeBtns = document.querySelectorAll('.size-btn');
    const btnShowMore = document.getElementById('btn-show-more');
    const priceContainer = document.getElementById('product-price-container');
    const stockDisplay = document.querySelector('#product-stock span');

    // THẺ HIỂN THỊ TÊN MÀU
    const selectedColorDisplay = document.getElementById('selected-color-name');

    // Hàm định dạng tiền tệ
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
    }

    finalColorBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            finalColorBtns.forEach(c => c.classList.remove('active'));
            this.classList.add('active');

            const selectedColor = this.getAttribute('data-color-name');

            // ---> IN TÊN MÀU LÊN MÀN HÌNH <---
            if (selectedColorDisplay) {
                selectedColorDisplay.innerText = selectedColor;
            }

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
            } else if (stockDisplay) {
                stockDisplay.innerText = "Hết hàng";
            }
        });
    });

    // 4. Xử lý khi chọn size: Cập nhật giá và SỐ LƯỢNG
    sizeBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                sizeBtns.forEach(s => s.classList.remove('active'));
                this.classList.add('active');

                // Lấy cả giá gốc và giá khuyến mãi từ data attribute
                const price = parseFloat(this.getAttribute('data-price'));
                const discountedPrice = parseFloat(this.getAttribute('data-discounted-price'));

                // Đọc loại khuyến mãi và giá trị từ thẻ bọc
                const promoType = this.getAttribute('data-promo-type');
                const promoValStr = this.getAttribute('data-promo-val');
                const promoVal = promoValStr ? parseFloat(promoValStr) : null;

                // Cập nhật giao diện Giá
                if (priceContainer) {
                    if (discountedPrice && discountedPrice < price) {

                        // Tạo text cho Badge dựa vào loại KM
                        let badgeText = '';
                        if (promoType === 'PERCENT') {
                            badgeText = `-${promoVal}%`;
                        } else if (promoVal) {
                            badgeText = `-${new Intl.NumberFormat('vi-VN').format(promoVal)}đ`;
                        }

                        priceContainer.innerHTML = `
                            <span class="pro-price-discounted">${formatCurrency(discountedPrice)}</span>
                            <del class="pro-price-original">${formatCurrency(price)}</del>
                            <span class="badge sale" style="padding: 4px 8px; font-size: 14px; border-radius: 4px; margin-left: 10px; background-color: #d32f2f; color: white;">${badgeText}</span>
                        `;
                    } else {
                        priceContainer.innerHTML = `
                            <span class="pro-price-regular">${formatCurrency(price)}</span>
                        `;
                    }
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