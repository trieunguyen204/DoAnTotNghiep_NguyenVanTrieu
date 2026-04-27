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

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             Model model) {
        List<OrderResponse> orders = orderService.getOrdersForAdminByStatus(status);

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status == null || status.isBlank() ? "ALL" : status);
        model.addAttribute("statuses", OrderStatus.values());

        return "admin/order/order_management";
    }

    @PostMapping("/approve/{id}")
    public String approveOrder(@PathVariable Long id,
                               @RequestParam(value = "currentStatus", required = false) String currentStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.approveOrder(id);
            redirectAttributes.addFlashAttribute("success", "Duyệt đơn hàng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }

    @PostMapping("/approve-selected")
    public String approveSelectedOrders(@RequestParam(value = "orderIds", required = false) List<Long> orderIds,
                                        @RequestParam(value = "currentStatus", required = false) String currentStatus,
                                        RedirectAttributes redirectAttributes) {
        try {
            if (orderIds == null || orderIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một đơn hàng.");
                return redirectByStatus(currentStatus);
            }

            int count = orderService.approveSelectedOrders(orderIds);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt " + count + " đơn hàng.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }

    @PostMapping("/approve-all")
    public String approveAllOrders(@RequestParam(value = "currentStatus", required = false) String currentStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            int count = orderService.approveAllOrdersByStatus(currentStatus);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt " + count + " đơn hàng.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus orderStatus,
                               @RequestParam(value = "currentStatus", required = false) String currentStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, orderStatus);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return redirectByStatus(currentStatus);
    }

    private String redirectByStatus(String currentStatus) {
        if (currentStatus == null || currentStatus.isBlank() || "ALL".equalsIgnoreCase(currentStatus)) {
            return "redirect:/admin/orders";
        }

        String cleanStatus = currentStatus.split(",")[0].trim().toUpperCase();

        return "redirect:/admin/orders?status=" + cleanStatus;
    }
}