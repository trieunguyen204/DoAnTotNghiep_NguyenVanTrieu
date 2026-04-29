package com.adidos.user.controller;

import com.adidos.user.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            forgotPasswordService.sendResetMail(email);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã gửi email reset mật khẩu"
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            Model model
    ) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }


    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }

            forgotPasswordService.resetPassword(token, password, passwordEncoder);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }
}