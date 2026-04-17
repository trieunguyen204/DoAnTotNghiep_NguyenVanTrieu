document.addEventListener("DOMContentLoaded", function() {
    const products = document.querySelectorAll('.product-card-premium');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = 1;
                entry.target.style.transform = "translateY(0)";
            }
        });
    }, { threshold: 0.1 });

    products.forEach(p => {
        p.style.opacity = 0;
        p.style.transform = "translateY(20px)";
        p.style.transition = "all 0.6s ease-out";
        observer.observe(p);
    });
});