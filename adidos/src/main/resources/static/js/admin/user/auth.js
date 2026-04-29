document.addEventListener("DOMContentLoaded", function () {
    initPasswordToggle();
    initResetPasswordValidation();
});

function initPasswordToggle() {
    const toggleButtons = document.querySelectorAll(".password-toggle-btn");

    toggleButtons.forEach(button => {
        button.addEventListener("click", function () {
            const input = button.parentElement.querySelector(".password-input");

            if (!input) return;

            if (input.type === "password") {
                input.type = "text";
                button.textContent = "🙈";
            } else {
                input.type = "password";
                button.textContent = "👁";
            }
        });
    });
}

function initResetPasswordValidation() {
    const form = document.querySelector(".reset-password-form");
    if (!form) return;

    const passwordInput = document.getElementById("newPassword");
    const confirmInput = document.getElementById("confirmPassword");
    const passwordRule = document.getElementById("passwordRule");
    const confirmRule = document.getElementById("confirmRule");

    function validate() {
        const password = passwordInput.value.trim();
        const confirm = confirmInput.value.trim();

        if (password.length >= 6) {
            passwordRule.textContent = "Mật khẩu hợp lệ";
            passwordRule.className = "password-rules valid";
        } else {
            passwordRule.textContent = "Mật khẩu tối thiểu 6 ký tự";
            passwordRule.className = "password-rules invalid";
        }

        if (!confirm) {
            confirmRule.textContent = "";
            confirmRule.className = "password-rules";
            return;
        }

        if (password === confirm) {
            confirmRule.textContent = "Mật khẩu xác nhận khớp";
            confirmRule.className = "password-rules valid";
        } else {
            confirmRule.textContent = "Mật khẩu xác nhận không khớp";
            confirmRule.className = "password-rules invalid";
        }
    }

    passwordInput.addEventListener("input", validate);
    confirmInput.addEventListener("input", validate);

    form.addEventListener("submit", function (e) {
        const password = passwordInput.value.trim();
        const confirm = confirmInput.value.trim();

        if (password.length < 6) {
            e.preventDefault();
            alert("Mật khẩu phải có tối thiểu 6 ký tự");
            return;
        }

        if (password !== confirm) {
            e.preventDefault();
            alert("Mật khẩu xác nhận không khớp");
        }
    });
}