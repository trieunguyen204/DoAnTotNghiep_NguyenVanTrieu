package com.adidos.user.controller;

import com.adidos.order.dto.OrderResponse;
import com.adidos.order.service.OrderService;
import com.adidos.user.dto.AddressRequest;
import com.adidos.user.dto.AddressResponse;
import com.adidos.user.dto.ProfileUpdateRequest;
import com.adidos.user.dto.UserResponse;
import com.adidos.user.service.AddressService;
import com.adidos.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;


@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;

    // 1. THÔNG TIN CÁ NHÂN
    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        String email = principal.getName();

        UserResponse user = userService.getProfileByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("activeMenu", "info");
        return "user/profile/info";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute ProfileUpdateRequest request,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        String email = principal.getName();

        try {
            userService.updateProfile(email, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    // 2. QUẢN LÝ ĐỊA CHỈ
    @GetMapping("/addresses")
    public String viewAddresses(Model model, Principal principal) {
        String email = principal.getName();
        // Sửa: Dùng getMyAddresses(email) thay vì tìm qua UserID
        List<AddressResponse> addresses = addressService.getMyAddresses(email);
        model.addAttribute("addresses", addresses);
        model.addAttribute("activeMenu", "addresses");
        return "user/profile/addresses";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@ModelAttribute AddressRequest request, Principal principal, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        addressService.addAddress(email, request);
        redirectAttributes.addFlashAttribute("success", "Thêm địa chỉ thành công!");
        return "redirect:/profile/addresses";
    }

    // Xóa địa chỉ
    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        addressService.deleteAddress(id, email);
        return "redirect:/profile/addresses";
    }

    // 3. QUẢN LÝ ĐƠN HÀNG
    @GetMapping("/orders")
    public String viewOrders(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String email = principal.getName();

        Page<OrderResponse> orderPage = orderService.getMyOrdersPage(email, page, size);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "orders");

        return "user/profile/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model, Principal principal) {
        String email = principal.getName();
        // Sửa: Dùng getOrderDetail(orderId, email) thay vì findById
        OrderResponse order = orderService.getOrderDetail(id, email);
        model.addAttribute("order", order);
        model.addAttribute("activeMenu", "orders");
        return "user/profile/order_detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        try {
            orderService.cancelOrder(id, email);
            redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/orders";
    }


    @PostMapping("/addresses/update/{id}")
    public String updateAddress(@PathVariable Long id,
                                @ModelAttribute AddressRequest request,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        String email = principal.getName();

        try {
            addressService.updateAddress(id, email, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/addresses";
    }

    @PostMapping("/addresses/default/{id}")
    public String setDefaultAddress(@PathVariable Long id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        String email = principal.getName();

        try {
            addressService.setDefaultAddress(id, email);
            redirectAttributes.addFlashAttribute("success", "Đã đặt địa chỉ mặc định!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/addresses";
    }
}