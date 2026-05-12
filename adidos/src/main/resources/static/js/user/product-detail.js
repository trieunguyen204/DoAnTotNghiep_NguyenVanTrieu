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


            // ===== HIỂN THỊ ẢNH THEO MÀU =====
            const allWrappers = document.querySelectorAll(".img-wrapper");

            // Ẩn toàn bộ trước
            allWrappers.forEach(wrapper => {
                wrapper.style.display = "none";
            });

            // Map unique ảnh
            const uniqueMap = new Map();

            allWrappers.forEach(wrapper => {

                const color =
                    wrapper.getAttribute("data-color-name");

                const img =
                    wrapper.querySelector("img");

                const src =
                    img?.getAttribute("src");

                if (
                    color === selectedColor &&
                    src &&
                    !uniqueMap.has(src)
                ) {
                    uniqueMap.set(src, wrapper);
                }
            });

            // Chỉ lấy ảnh unique
            const currentImages = Array.from(uniqueMap.values());

            const DEFAULT_VISIBLE = 4;

            // Hiện 4 ảnh đầu
            currentImages.forEach((wrapper, index) => {

                if (index < DEFAULT_VISIBLE) {
                    wrapper.style.display = "";
                }

            });

            // SHOW MORE
            btnShowMore.style.display =
                currentImages.length > DEFAULT_VISIBLE
                    ? "block"
                    : "none";

            btnShowMore.onclick = null;

            if (currentImages.length > DEFAULT_VISIBLE) {

                btnShowMore.onclick = () => {

                    currentImages
                        .slice(DEFAULT_VISIBLE)
                        .forEach(wrapper => {
                            wrapper.style.display = "";
                        });

                    btnShowMore.style.display = "none";
                };
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


    /* ===== HOVER ZOOM THEO CHUỘT ===== */
    document.addEventListener("mousemove", function (e) {

        const wrapper = e.target.closest(".img-wrapper");

        if (!wrapper) return;

        const img = wrapper.querySelector(".gallery-img");

        if (!img) return;

        const rect = wrapper.getBoundingClientRect();

        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;

        img.style.transformOrigin = `${x}% ${y}%`;
    });


    /* ===== MODAL ZOOM ===== */

    const zoomModal =
        document.getElementById("imageZoomModal");

    const zoomMainImg =
        document.getElementById("zoomMainImg");


    /* ===== ZOOM THEO CHUỘT TRONG MODAL ===== */
    zoomMainImg?.addEventListener("mousemove", function (e) {

        const rect = this.getBoundingClientRect();

        const x =
            ((e.clientX - rect.left) / rect.width) * 100;

        const y =
            ((e.clientY - rect.top) / rect.height) * 100;

        this.style.transformOrigin = `${x}% ${y}%`;
    });

    zoomMainImg?.addEventListener("mouseleave", function () {

        this.style.transformOrigin = "center center";
    });

    const zoomThumbnails =
        document.getElementById("zoomThumbnails");

    const zoomClose =
        document.getElementById("zoomClose");

    document.addEventListener("keydown", function (e) {

            if (
                e.key === "Escape" &&
                zoomModal?.classList.contains("show")
            ) {

                zoomModal.classList.remove("show");

                document.body.style.overflow = "";
            }
        });


    function openImageZoom(clickedImg) {

        if (!zoomModal) return;

        const clickedWrapper =
            clickedImg.closest(".img-wrapper");

        const currentColor =
            clickedWrapper?.getAttribute("data-color-name");

        const visibleImages =
            Array.from(document.querySelectorAll(".img-wrapper"))
                .filter(wrapper =>
                    wrapper.getAttribute("data-color-name") === currentColor &&
                    wrapper.style.display !== "none"
                )
                .map(wrapper =>
                    wrapper.querySelector(".gallery-img")
                )
                .filter(Boolean);

        const unique = new Set();

        const images = visibleImages.filter(img => {

            const src = img.getAttribute("src");

            if (!src || unique.has(src)) {
                return false;
            }

            unique.add(src);

            return true;
        });

        zoomMainImg.src =
            clickedImg.getAttribute("src");

        zoomThumbnails.innerHTML = "";

        images.forEach(img => {

            const src = img.getAttribute("src");

            const thumb =
                document.createElement("button");

            thumb.type = "button";

            thumb.className = "zoom-thumb";

            if (src === zoomMainImg.src) {
                thumb.classList.add("active");
            }

            thumb.innerHTML =
                `<img src="${src}" alt="">`;

            thumb.addEventListener("click", () => {

                zoomMainImg.src = src;

                document
                    .querySelectorAll(".zoom-thumb")
                    .forEach(t =>
                        t.classList.remove("active")
                    );

                thumb.classList.add("active");
            });

            zoomThumbnails.appendChild(thumb);
        });

        zoomModal.classList.add("show");

        document.body.style.overflow = "hidden";
    }


    document.addEventListener("click", function (e) {

        const img =
            e.target.closest(".gallery-grid .gallery-img");

        if (!img) return;

        openImageZoom(img);
    });


    zoomClose?.addEventListener("click", () => {

        zoomModal.classList.remove("show");

        document.body.style.overflow = "";
    });


    zoomModal?.addEventListener("click", function (e) {

        if (e.target === zoomModal) {

            zoomModal.classList.remove("show");

            document.body.style.overflow = "";
        }
    });

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


    const btnAddToCart = document.querySelector('.btn-add-to-cart');

    if (btnAddToCart) {
        if (btnAddToCart.dataset.listenerAttached) return;
        btnAddToCart.dataset.listenerAttached = 'true';


        btnAddToCart.addEventListener('click', function() {
            const activeSizeBtn = document.querySelector('.size-btn.active');

            if (!activeSizeBtn) {
                alert('Vui lòng chọn kích cỡ!');
                return;
            }

            const variantId = activeSizeBtn.getAttribute('data-variant-id');
            const requestData = {
                productVariantId: parseInt(variantId),
                quantity: 1
            };

            // 1. Khóa nút ngay khi vừa click để chống click đúp
            btnAddToCart.disabled = true;
            btnAddToCart.style.opacity = '0.7'; // Làm mờ nút đi một chút
            btnAddToCart.innerText = "Đang xử lý..."; // Đổi text để người dùng biết

            fetch('/api/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'same-origin',
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                const data = await response.text();

                if (data.includes('<!DOCTYPE html>')) throw new Error('API bị chặn bởi SecurityConfig');
                if (!response.ok) throw new Error(data);
                return data;
            })
            .then(data => {
                alert(data);
                if (window.updateCartBadge) window.updateCartBadge();
            })
            .catch(error => {
                alert('Lỗi: ' + error.message);
            })
            .finally(() => {
                // 2. Mở khóa lại nút sau khi API chạy xong (dù thành công hay thất bại)
                btnAddToCart.disabled = false;
                btnAddToCart.style.opacity = '1';
                btnAddToCart.innerText = "THÊM VÀO GIỎ HÀNG"; // Trả lại text ban đầu
            });
        });
    }

    document.querySelectorAll(".product-accordion-toggle").forEach(button => {
        button.addEventListener("click", function () {
            const accordion = this.closest(".product-accordion");
            accordion.classList.toggle("open");
        });
    });


});