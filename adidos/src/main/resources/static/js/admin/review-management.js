document.addEventListener("DOMContentLoaded", function () {
    const replyForms = document.querySelectorAll(".reply-form");

    replyForms.forEach(form => {
        form.addEventListener("submit", function (e) {
            if (!confirm("Gửi phản hồi cho đánh giá này?")) {
                e.preventDefault();
            }
        });
    });
});