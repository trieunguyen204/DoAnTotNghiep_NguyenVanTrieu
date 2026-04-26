document.addEventListener("DOMContentLoaded", function() {
    const removeBtns = document.querySelectorAll('.btn-remove-item');

    removeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

            const itemId = this.getAttribute('data-id');

            fetch(`/api/cart/remove/${itemId}`, {
                method: 'DELETE',
                credentials: 'same-origin'
            })
            .then(response => {
                if (response.ok) {
                    // Xóa dòng tr trên giao diện
                    document.getElementById(`cart-item-${itemId}`).remove();
                    // Cập nhật lại số lượng trên header
                    if (window.updateCartBadge) window.updateCartBadge();

                    // Nếu hết sản phẩm thì reload để hiện "Giỏ hàng trống"
                    if (document.querySelectorAll('.btn-remove-item').length === 0) {
                        window.location.reload();
                    }
                } else {
                    alert('Lỗi khi xóa sản phẩm');
                }
            });
        });
    });


    // Thêm vào dưới đoạn bắt sự kiện nút "Xóa"
    const qtyBtns = document.querySelectorAll('.btn-qty');
    qtyBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const itemId = this.getAttribute('data-id');
            const input = document.getElementById(`qty-${itemId}`);
            let newQty = parseInt(input.value);

            if (this.classList.contains('btn-plus')) newQty++;
            else if (this.classList.contains('btn-minus')) newQty--;

            if (newQty < 1) return; // Nếu < 1 thì chặn, muốn xóa thì bấm nút Xóa

            fetch(`/api/cart/update/${itemId}?quantity=${newQty}`, {
                method: 'PUT',
                credentials: 'same-origin'
            }).then(async response => {
                if (response.ok) {
                    window.location.reload(); // Tải lại để cập nhật tổng tiền
                } else {
                    alert('Lỗi: ' + await response.text());
                }
            });
        });
    });
});