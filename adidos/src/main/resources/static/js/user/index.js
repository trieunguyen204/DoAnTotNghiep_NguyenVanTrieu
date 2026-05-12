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



    document.querySelectorAll(".filter-toggle").forEach(button => {
            button.addEventListener("click", function () {
                const dropdown = this.closest(".filter-dropdown");
                dropdown.classList.toggle("open");
            });
        });



});