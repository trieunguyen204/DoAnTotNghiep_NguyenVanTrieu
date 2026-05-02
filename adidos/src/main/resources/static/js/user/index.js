document.addEventListener("DOMContentLoaded", function () {

    /* ===== ANIMATION CARD ===== */
    const products = document.querySelectorAll(".product-card-premium");

    if ("IntersectionObserver" in window) {
        const observer = new IntersectionObserver((entries, obs) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = "1";
                    entry.target.style.transform = "translateY(0)";
                    obs.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        products.forEach(card => {
            card.style.opacity = "0";
            card.style.transform = "translateY(20px)";
            card.style.transition = "opacity 0.6s ease-out, transform 0.6s ease-out";
            observer.observe(card);
        });
    } else {
        products.forEach(card => {
            card.style.opacity = "1";
            card.style.transform = "translateY(0)";
        });
    }

    /* ===== FORMAT BADGE DISCOUNT FIXED AMOUNT ===== */
    document.querySelectorAll(".badge.sale[data-discount]").forEach(el => {
        const rawValue = el.dataset.discount;

        if (!rawValue) return;

        const value = Number(
            rawValue
                .toString()
                .replace(/[^\d.-]/g, "")
        );

        if (Number.isNaN(value) || value <= 0) return;

        let text;

        if (value >= 1000000) {
            const million = value / 1000000;
            text = "-" + (Number.isInteger(million) ? million : million.toFixed(1)) + "tr";
        } else if (value >= 1000) {
            text = "-" + Math.floor(value / 1000) + "k";
        } else {
            text = "-" + value.toLocaleString("vi-VN") + "đ";
        }

        el.textContent = text;
    });

});