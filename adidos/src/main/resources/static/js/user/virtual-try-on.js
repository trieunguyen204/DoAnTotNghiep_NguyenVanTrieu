document.addEventListener("DOMContentLoaded", function () {
    const openBtn = document.getElementById("open-try-on-modal");
    const historyBtn = document.getElementById("open-try-on-history");

    const modal = document.getElementById("try-on-modal");
    const closeBtn = document.getElementById("close-try-on-modal");

    const form = document.getElementById("try-on-form");
    const fileInput = document.getElementById("try-on-person-image");
    const previewImg = document.getElementById("try-on-preview-img");
    const messageBox = document.getElementById("try-on-message");

    const resultBox = document.getElementById("try-on-result-box");
    const resultsGrid = document.getElementById("try-on-results-grid");

    const historyBox = document.getElementById("try-on-history-box");
    const historyGrid = document.getElementById("try-on-history-grid");

    if (!openBtn || !modal || !form) {
        console.warn("Thiếu DOM thử đồ AI");
        return;
    }

    openBtn.addEventListener("click", function () {
        const variantId = getSelectedVariantId();

        if (!variantId) {
            alert("Vui lòng chọn size trước khi thử đồ");
            return;
        }

        openModal();
        showTryForm();
    });

    historyBtn?.addEventListener("click", async function () {
        openModal();
        hideTryResults();
        await loadTryOnHistory();
    });

    closeBtn?.addEventListener("click", closeModal);

    modal.addEventListener("click", function (e) {
        if (e.target === modal) {
            closeModal();
        }
    });

    fileInput?.addEventListener("change", function () {
        const file = fileInput.files[0];

        if (!file) return;

        if (!file.type.startsWith("image/")) {
            alert("Vui lòng chọn file ảnh");
            fileInput.value = "";
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert("Ảnh không được vượt quá 5MB");
            fileInput.value = "";
            return;
        }

        if (previewImg) {
            previewImg.src = URL.createObjectURL(file);
            previewImg.style.display = "block";
        }
    });

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const productId = getProductId();
        const variantId = getSelectedVariantId();
        const file = fileInput?.files[0];

        if (!productId) {
            showMessage("Không tìm thấy productId", true);
            return;
        }

        if (!variantId || variantId === "null" || variantId === "undefined") {
            showMessage("Vui lòng chọn size trước khi thử đồ", true);
            return;
        }

        if (!file) {
            showMessage("Vui lòng upload ảnh của bạn", true);
            return;
        }

        const formData = new FormData();
        formData.append("productId", productId);
        formData.append("variantId", variantId);
        formData.append("personImage", file);

        try {
            setLoading(true);
            showMessage("AI đang thử toàn bộ màu của sản phẩm, vui lòng chờ...");
            clearResults();
            hideHistory();

            const response = await fetch("/api/try-on", {
                method: "POST",
                body: formData,
                credentials: "same-origin"
            });

            const text = await response.text();
            const data = parseJson(text);

            if (!response.ok) {
                throw new Error(data.message || "Thử đồ thất bại");
            }

            const colorResults = Array.isArray(data.colorResults)
                ? data.colorResults
                : [];

            if (colorResults.length > 0) {
                renderColorResults(colorResults);
            } else {
                renderFallbackSingleResult(data);
            }

            if (resultBox) {
                resultBox.style.display = "block";
            }

            showMessage("Thử đồ thành công. Hãy chọn màu bạn thấy hợp nhất.", false);

        } catch (error) {
            console.error(error);
            showMessage(error.message, true);
        } finally {
            setLoading(false);
        }
    });

    async function loadTryOnHistory() {
        const productId = getProductId();

        if (!productId) {
            showMessage("Không tìm thấy productId", true);
            return;
        }

        try {
            showMessage("Đang tải lịch sử thử đồ...");
            setHistoryLoading(true);

            const response = await fetch(`/api/try-on/history?productId=${encodeURIComponent(productId)}`, {
                method: "GET",
                credentials: "same-origin"
            });

            const text = await response.text();
            const data = parseJson(text);

            if (!response.ok) {
                throw new Error(data.message || "Không lấy được lịch sử thử đồ");
            }

            renderHistory(data);
            showMessage("");

        } catch (error) {
            console.error(error);
            showMessage(error.message, true);
        } finally {
            setHistoryLoading(false);
        }
    }

    function renderColorResults(colorResults) {
        if (!resultsGrid) return;

        resultsGrid.innerHTML = "";

        colorResults.forEach(item => {
            const card = buildResultCard(item, "Chọn màu này");
            resultsGrid.appendChild(card);
        });

        bindChooseColorButtons(resultsGrid);
    }

    function renderHistory(items) {
        if (!historyGrid || !historyBox) return;

        historyGrid.innerHTML = "";

        if (!Array.isArray(items) || items.length === 0) {
            historyGrid.innerHTML = `
                <div class="try-on-empty-history">
                    Chưa có lịch sử thử đồ cho sản phẩm này.
                </div>
            `;
            historyBox.style.display = "block";
            return;
        }

        items.forEach(item => {
            if (!item.resultImageUrl) return;

            const card = buildResultCard(item, "Chọn lại màu này");
            historyGrid.appendChild(card);
        });

        historyBox.style.display = "block";
        bindChooseColorButtons(historyGrid);
    }

    function renderFallbackSingleResult(data) {
        if (!resultsGrid) return;

        resultsGrid.innerHTML = "";

        const item = {
            variantId: data.variantId,
            colorName: data.colorName,
            sizeName: data.sizeName,
            resultImageUrl: data.resultImageUrl || data.personImageUrl,
            status: data.status,
            errorMessage: data.errorMessage
        };

        const card = buildResultCard(item, "Chọn màu này");
        resultsGrid.appendChild(card);

        bindChooseColorButtons(resultsGrid);
    }

    function buildResultCard(item, buttonText) {
        const card = document.createElement("div");
        card.className = "try-on-result-item";

        if (item.status === "FAILED") {
            card.classList.add("failed");
        }

        const colorName = item.colorName || "Màu khác";
        const sizeName = item.sizeName ? `Size ${item.sizeName}` : "";

        const imageHtml = item.resultImageUrl
            ? `<img src="${escapeHtml(item.resultImageUrl)}" alt="Kết quả thử đồ ${escapeHtml(colorName)}">`
            : `<div class="try-on-result-failed">Không tạo được ảnh</div>`;

        const errorHtml = item.errorMessage
            ? `<p class="try-on-result-error">${escapeHtml(item.errorMessage)}</p>`
            : "";

        const buttonHtml = item.resultImageUrl && item.variantId
            ? `
                <button
                    type="button"
                    class="try-this-color-btn"
                    data-variant-id="${escapeHtml(String(item.variantId))}">
                    ${escapeHtml(buttonText)}
                </button>
            `
            : "";

        card.innerHTML = `
            <div class="try-on-color-name">
                ${escapeHtml(colorName)}
                ${sizeName ? `<span>${escapeHtml(sizeName)}</span>` : ""}
            </div>

            ${imageHtml}
            ${errorHtml}
            ${buttonHtml}
        `;

        return card;
    }

    function bindChooseColorButtons(container) {
        container.querySelectorAll(".try-this-color-btn").forEach(button => {
            button.addEventListener("click", function () {
                const variantId = this.getAttribute("data-variant-id");
                selectVariantById(variantId);
                showMessage("Đã chọn màu này cho sản phẩm.", false);
            });
        });
    }

    function selectVariantById(variantId) {
        if (!variantId) return;

        if (window.selectProductVariantById) {
            const ok = window.selectProductVariantById(variantId);

            if (!ok) {
                console.warn("Không chọn được variant:", variantId);
            }

            return;
        }

        const targetSize = document.querySelector(
            `.size-btn[data-variant-id="${CSS.escape(String(variantId))}"]`
        );

        if (!targetSize) {
            console.warn("Không tìm thấy variant trên giao diện:", variantId);
            return;
        }

        targetSize.click();
        window.selectedVariantId = variantId;
    }

    function showTryForm() {
        hideHistory();
        showMessage("");
    }

    function openModal() {
        modal.style.display = "flex";
        document.body.style.overflow = "hidden";
    }

    function closeModal() {
        modal.style.display = "none";
        document.body.style.overflow = "";
    }

    function clearResults() {
        if (resultsGrid) {
            resultsGrid.innerHTML = "";
        }

        if (resultBox) {
            resultBox.style.display = "none";
        }
    }

    function hideTryResults() {
        if (resultBox) {
            resultBox.style.display = "none";
        }
    }

    function hideHistory() {
        if (historyGrid) {
            historyGrid.innerHTML = "";
        }

        if (historyBox) {
            historyBox.style.display = "none";
        }
    }

    function setLoading(isLoading) {
        const submitBtn = form.querySelector(".try-on-submit");

        if (submitBtn) {
            submitBtn.disabled = isLoading;
            submitBtn.textContent = isLoading
                ? "Đang thử toàn bộ màu..."
                : "Bắt đầu thử đồ";
        }
    }

    function setHistoryLoading(isLoading) {
        if (!historyBtn) return;

        historyBtn.disabled = isLoading;
        historyBtn.textContent = isLoading
            ? "Đang tải lịch sử..."
            : "🕘 Xem lịch sử thử đồ";
    }

    function getSelectedVariantId() {
        const activeSize = document.querySelector(".size-btn.active");
        const id = activeSize?.getAttribute("data-variant-id") || window.selectedVariantId;
        return id && id !== "null" && id !== "undefined" ? id : null;
    }

    function getProductId() {
        return openBtn.getAttribute("data-product-id")
            || historyBtn?.getAttribute("data-product-id");
    }

    function showMessage(message, isError = false) {
        if (!messageBox) return;

        messageBox.textContent = message;
        messageBox.style.display = message ? "block" : "none";
        messageBox.classList.toggle("error", isError);
        messageBox.style.color = isError ? "#dc2626" : "#16a34a";
    }

    function parseJson(text) {
        try {
            return JSON.parse(text);
        } catch {
            console.error("Backend không trả JSON:", text);
            throw new Error("Backend không trả JSON. Kiểm tra Spring Boot console.");
        }
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});