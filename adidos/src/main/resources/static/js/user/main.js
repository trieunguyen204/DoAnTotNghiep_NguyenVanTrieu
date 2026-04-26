document.addEventListener("DOMContentLoaded", function() {

    const profileBtn = document.querySelector('.profile-dropdown-container > a');
    const dropdownMenu = document.querySelector('.profile-dropdown-container .dropdown-menu');

    // Click vào icon để mở/đóng menu
    if (profileBtn && dropdownMenu) {
        profileBtn.addEventListener('click', function(e) {
            e.preventDefault(); // Ngăn chuyển trang
            dropdownMenu.classList.toggle('show');
        });
    }

    // Click ra ngoài khoảng trống để đóng menu
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.profile-dropdown-container')) {
            if (dropdownMenu && dropdownMenu.classList.contains('show')) {
                dropdownMenu.classList.remove('show');
            }
        }
    });

    // Hiệu ứng Sticky Header
    const header = document.getElementById('main-header');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });

    // Khai báo global để có thể gọi ở các file js khác
    window.updateCartBadge = function() {
        const timestamp = new Date().getTime();
        fetch(`/api/cart/count?t=${timestamp}`, {
            method: 'GET',
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) throw new Error("Not logged in or error");
            return response.json();
        })
        .then(count => {
            const badge = document.querySelector('.cart-badge');
            if (badge) {
                badge.innerText = count;
                badge.style.display = count > 0 ? 'inline-block' : 'none';
            }
        })
        .catch(err => console.log('Không thể lấy số lượng giỏ hàng:', err));
    };


    updateCartBadge();
});