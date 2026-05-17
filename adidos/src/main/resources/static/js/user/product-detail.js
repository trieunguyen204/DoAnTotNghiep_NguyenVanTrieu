document.addEventListener("DOMContentLoaded", function() {

    let selectedVariantId = null;

    const colorBtns = document.querySelectorAll('.color-btn');
    const uniqueColors = new Set();

    colorBtns.forEach(btn => {
        const colorName = btn.getAttribute('data-color-name');
        if (uniqueColors.has(colorName)) {
            btn.remove();
        } else {
            uniqueColors.add(colorName);
        }
    });

    const finalColorBtns = document.querySelectorAll('.color-btn');
    const sizeBtns = document.querySelectorAll('.size-btn');
    const btnShowMore = document.getElementById('btn-show-more');
    const priceContainer = document.getElementById('product-price-container');
    const stockDisplay = document.querySelector('#product-stock span');
    const selectedColorDisplay = document.getElementById('selected-color-name');

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
    }

    finalColorBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            finalColorBtns.forEach(c => c.classList.remove('active'));
            this.classList.add('active');

            const selectedColor = this.getAttribute('data-color-name');

            if (selectedColorDisplay) {
                selectedColorDisplay.innerText = selectedColor;
            }

            const allWrappers = document.querySelectorAll(".img-wrapper");

            allWrappers.forEach(wrapper => {
                wrapper.style.display = "none";
            });

            const uniqueMap = new Map();

            allWrappers.forEach(wrapper => {
                const color = wrapper.getAttribute("data-color-name");
                const img = wrapper.querySelector("img");
                const src = img?.getAttribute("src");

                if (color === selectedColor && src && !uniqueMap.has(src)) {
                    uniqueMap.set(src, wrapper);
                }
            });

            const currentImages = Array.from(uniqueMap.values());
            const DEFAULT_VISIBLE = 4;

            currentImages.forEach((wrapper, index) => {
                if (index < DEFAULT_VISIBLE) {
                    wrapper.style.display = "";
                }
            });

            if (btnShowMore) {
                btnShowMore.style.display =
                    currentImages.length > DEFAULT_VISIBLE ? "block" : "none";

                btnShowMore.onclick = null;

                if (currentImages.length > DEFAULT_VISIBLE) {
                    btnShowMore.onclick = () => {
                        currentImages.slice(DEFAULT_VISIBLE).forEach(wrapper => {
                            wrapper.style.display = "";
                        });

                        btnShowMore.style.display = "none";
                    };
                }
            }

            let firstVisibleSize = null;

            sizeBtns.forEach(size => {
                size.classList.remove('active');

                if (size.getAttribute('data-color-name') === selectedColor) {
                    size.classList.remove('hidden');
                    if (!firstVisibleSize) firstVisibleSize = size;
                } else {
                    size.classList.add('hidden');
                }
            });

            if (firstVisibleSize) {
                firstVisibleSize.click();
            } else if (stockDisplay) {
                stockDisplay.innerText = "Hết hàng";
            }
        });
    });

    sizeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            selectedVariantId = this.dataset.variantId;
            window.selectedVariantId = selectedVariantId;

            console.log("Selected Variant:", selectedVariantId);

            sizeBtns.forEach(s => s.classList.remove('active'));
            this.classList.add('active');

            const price = parseFloat(this.getAttribute('data-price'));
            const discountedPrice = parseFloat(this.getAttribute('data-discounted-price'));

            const promoType = this.getAttribute('data-promo-type');
            const promoValStr = this.getAttribute('data-promo-val');
            const promoVal = promoValStr ? parseFloat(promoValStr) : null;

            if (priceContainer) {
                if (discountedPrice && discountedPrice < price) {
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

            const stock = this.getAttribute('data-stock');

            if (stockDisplay) {
                stockDisplay.innerText = (stock > 0) ? stock : "Hết hàng";
            }
        });
    });

    if (finalColorBtns.length > 0) finalColorBtns[0].click();

    document.addEventListener("mousemove", function(e) {
        const wrapper = e.target.closest(".img-wrapper");
        if (!wrapper) return;

        const img = wrapper.querySelector(".gallery-img");
        if (!img) return;

        const rect = wrapper.getBoundingClientRect();

        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;

        img.style.transformOrigin = `${x}% ${y}%`;
    });

    const zoomModal = document.getElementById("imageZoomModal");
    const zoomMainImg = document.getElementById("zoomMainImg");
    const zoomThumbnails = document.getElementById("zoomThumbnails");
    const zoomClose = document.getElementById("zoomClose");

    zoomMainImg?.addEventListener("mousemove", function(e) {
        const rect = this.getBoundingClientRect();

        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;

        this.style.transformOrigin = `${x}% ${y}%`;
    });

    zoomMainImg?.addEventListener("mouseleave", function() {
        this.style.transformOrigin = "center center";
    });

    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape" && zoomModal?.classList.contains("show")) {
            zoomModal.classList.remove("show");
            document.body.style.overflow = "";
        }
    });

    function openImageZoom(clickedImg) {
        if (!zoomModal) return;

        const clickedWrapper = clickedImg.closest(".img-wrapper");
        const currentColor = clickedWrapper?.getAttribute("data-color-name");

        const visibleImages =
            Array.from(document.querySelectorAll(".img-wrapper"))
                .filter(wrapper =>
                    wrapper.getAttribute("data-color-name") === currentColor &&
                    wrapper.style.display !== "none"
                )
                .map(wrapper => wrapper.querySelector(".gallery-img"))
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

        zoomMainImg.src = clickedImg.getAttribute("src");
        zoomThumbnails.innerHTML = "";

        images.forEach(img => {
            const src = img.getAttribute("src");

            const thumb = document.createElement("button");
            thumb.type = "button";
            thumb.className = "zoom-thumb";

            if (src === zoomMainImg.src) {
                thumb.classList.add("active");
            }

            thumb.innerHTML = `<img src="${src}" alt="">`;

            thumb.addEventListener("click", () => {
                zoomMainImg.src = src;

                document.querySelectorAll(".zoom-thumb").forEach(t =>
                    t.classList.remove("active")
                );

                thumb.classList.add("active");
            });

            zoomThumbnails.appendChild(thumb);
        });

        zoomModal.classList.add("show");
        document.body.style.overflow = "hidden";
    }

    document.addEventListener("click", function(e) {
        const img = e.target.closest(".gallery-grid .gallery-img");

        if (!img) return;

        openImageZoom(img);
    });

    zoomClose?.addEventListener("click", () => {
        zoomModal.classList.remove("show");
        document.body.style.overflow = "";
    });

    zoomModal?.addEventListener("click", function(e) {
        if (e.target === zoomModal) {
            zoomModal.classList.remove("show");
            document.body.style.overflow = "";
        }
    });

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

            btnAddToCart.disabled = true;
            btnAddToCart.style.opacity = '0.7';
            btnAddToCart.innerText = "Đang xử lý...";

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

                    if (data.includes('<!DOCTYPE html>')) {
                        throw new Error('API bị chặn bởi SecurityConfig');
                    }

                    if (!response.ok) {
                        throw new Error(data);
                    }

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
                    btnAddToCart.disabled = false;
                    btnAddToCart.style.opacity = '1';
                    btnAddToCart.innerText = "THÊM VÀO GIỎ HÀNG";
                });
        });
    }

    document.querySelectorAll(".product-accordion-toggle").forEach(button => {
        button.addEventListener("click", function() {
            const accordion = this.closest(".product-accordion");
            accordion.classList.toggle("open");
        });
    });

    window.selectProductVariantById = function(variantId) {
        if (!variantId) return false;

        const targetSize = document.querySelector(
            `.size-btn[data-variant-id="${CSS.escape(String(variantId))}"]`
        );

        if (!targetSize) {
            console.warn("Không tìm thấy size-btn cho variant:", variantId);
            return false;
        }

        const colorName = targetSize.getAttribute("data-color-name");

        const colorBtn = Array.from(document.querySelectorAll(".color-btn"))
            .find(btn => btn.getAttribute("data-color-name") === colorName);

        if (colorBtn) {
            colorBtn.click();
        }

        targetSize.click();

        window.selectedVariantId = variantId;

        return true;
    };

});