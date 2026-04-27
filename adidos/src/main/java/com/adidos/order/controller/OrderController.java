package com.adidos.order.controller;

import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.service.CartService;
import com.adidos.order.dto.CheckoutRequest;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.service.OrderService;
import com.adidos.order.service.PayOSService;
import com.adidos.user.dto.AddressResponse;
import com.adidos.user.service.AddressService;
import com.adidos.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final PayOSService payOSService;
    private final VoucherService voucherService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        String email = principal.getName();

        List<CartItemResponse> cartItems = cartService.getCartByUser(email, true);
        List<AddressResponse> addresses = addressService.getMyAddresses(email);
        model.addAttribute("vouchers", voucherService.getAvailableVouchers());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        AddressResponse defaultAddress = addresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = new BigDecimal("30000");
        BigDecimal finalAmount = totalPrice.add(shippingFee);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("finalAmount", finalAmount);
        model.addAttribute("checkoutRequest", new CheckoutRequest());

        return "order/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@ModelAttribute CheckoutRequest request,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            Long orderId = orderService.placeOrder(principal.getName(), request);

            if ("COD".equals(request.getPaymentMethod())) {
                return "redirect:/checkout/success/" + orderId;
            }

            if ("QR_MANUAL".equals(request.getPaymentMethod())) {
                return "redirect:/payment/qr/" + orderId;
            }

            if ("PAYOS".equals(request.getPaymentMethod())) {
                String checkoutUrl = payOSService.createPaymentLink(orderId);
                return "redirect:" + checkoutUrl;
            }

            return "redirect:/checkout/success/" + orderId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success/{id}")
    public String checkoutSuccess(@PathVariable Long id,
                                  Model model,
                                  Principal principal) {
        OrderResponse order = orderService.getOrderDetail(id, principal.getName());
        model.addAttribute("order", order);
        return "order/success";
    }

    @GetMapping("/payment/qr/{id}")
    public String qrPayment(@PathVariable Long id,
                            Model model,
                            Principal principal) {
        OrderResponse order = orderService.getOrderDetail(id, principal.getName());
        model.addAttribute("order", order);
        return "order/qr_payment";
    }

    @GetMapping("/payment/payos/{id}")
    public String payosPayment(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "PayOS sẽ tích hợp ở bước tiếp theo.");
        return "redirect:/checkout/success/" + id;
    }

    @GetMapping("/payment/payos/return")
    public String payosReturn(@RequestParam Long orderId,
                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Thanh toán PayOS đang được xác nhận.");
        return "redirect:/checkout/success/" + orderId;
    }

    @GetMapping("/payment/payos/cancel")
    public String payosCancel(@RequestParam Long orderId,
                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Bạn đã hủy thanh toán PayOS.");
        return "redirect:/checkout/success/" + orderId;
    }

    @GetMapping("/api/vouchers/check")
    @ResponseBody
    public ResponseEntity<?> checkVoucher(@RequestParam String code,
                                          @RequestParam BigDecimal total) {
        try {
            BigDecimal discount = voucherService.calculateDiscount(code, total);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "discountAmount", discount,
                    "message", "Áp dụng mã giảm giá thành công!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "discountAmount", BigDecimal.ZERO,
                    "message", e.getMessage()
            ));
        }
    }


}