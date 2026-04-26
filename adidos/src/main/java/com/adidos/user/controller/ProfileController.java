package com.adidos.user.controller;

import com.adidos.order.service.OrderService;
import com.adidos.user.dto.AddressRequest;
import com.adidos.user.dto.ProfileUpdateRequest;
import com.adidos.user.service.AddressService;
import com.adidos.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;

    // 1. Quản lý tài khoản cá nhân
    @GetMapping
    public String viewProfile(Model model, Principal principal) {

        return "user/profile/info"; // Trả về file giao diện
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute ProfileUpdateRequest request, Principal principal, RedirectAttributes redirectAttributes) {

        return "redirect:/profile";
    }

    // 2. Quản lý địa chỉ nhận hàng
    @GetMapping("/addresses")
    public String viewAddresses(Model model, Principal principal) {

        return "user/profile/addresses";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@ModelAttribute AddressRequest request, Principal principal) {

        return "redirect:/profile/addresses";
    }

    // 3. Lịch sử mua hàng
    @GetMapping("/orders")
    public String viewOrders(Model model, Principal principal) {

        return "user/profile/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model, Principal principal) {

        return "user/profile/order_detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {

        return "redirect:/profile/orders";
    }
}