package com.adidos.user.controller;

import com.adidos.user.dto.RegisterAfterOrderRequest;
import com.adidos.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegisterAfterOrderController {

    private final UserService userService;

    @GetMapping("/register-after-order")
    public String showForm(@RequestParam Long orderId, Model model) {
        RegisterAfterOrderRequest request = new RegisterAfterOrderRequest();
        request.setOrderId(orderId);

        model.addAttribute("request", request);
        return "auth/register_after_order";
    }

    @PostMapping("/register-after-order")
    public String register(@ModelAttribute RegisterAfterOrderRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.registerAfterOrder(request);
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản thành công. Bạn có thể quản lý đơn hàng tại đây.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register-after-order?orderId=" + request.getOrderId();
        }
    }
}