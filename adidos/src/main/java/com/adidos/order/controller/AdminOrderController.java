package com.adidos.order.controller;

import com.adidos.order.dto.OrderResponse;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;


    @PostMapping("/next-status/{id}")
    public String nextStatus(@PathVariable Long id,
                             @RequestParam(value = "currentStatus", required = false) String currentStatus,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.moveToNextStatus(id);
            redirectAttributes.addFlashAttribute("success", "Đã chuyển trạng thái đơn hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id,
                              @RequestParam(value = "currentStatus", required = false) String currentStatus,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrderByAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        model.addAttribute("orders", orderService.getAllOrders(status));
        model.addAttribute("currentStatus", status == null ? "ALL" : status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/order_management";
    }

    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable Long id,
                              @RequestParam(value = "status", required = false) String status,
                              Model model) {
        model.addAttribute("order", orderService.getAdminOrderDetail(id));
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/order_detail";
    }

    private String redirectByStatus(String currentStatus) {
        if (currentStatus == null || currentStatus.isBlank() || "ALL".equalsIgnoreCase(currentStatus)) {
            return "redirect:/admin/orders";
        }

        return "redirect:/admin/orders?status=" + currentStatus.split(",")[0].trim().toUpperCase();
    }
}
