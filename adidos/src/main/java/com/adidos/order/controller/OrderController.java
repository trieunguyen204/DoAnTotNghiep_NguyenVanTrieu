package com.adidos.order.controller;

import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.service.CartService;
import com.adidos.order.dto.CheckoutRequest;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.service.OrderService;
import com.adidos.order.service.PayOSService;
import com.adidos.user.dto.AddressResponse;
import com.adidos.user.service.AddressService;
import com.adidos.voucher.VoucherService;
import jakarta.servlet.http.HttpSession;
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
    public String checkoutPage(Model model, Principal principal, HttpSession session) {

        boolean isLogged = principal != null;
        String identifier = isLogged ? principal.getName() : session.getId();

        List<CartItemResponse> cartItems = isLogged
                ? cartService.getCartByUser(principal.getName(), true)
                : cartService.getCartByUser(session.getId(), false);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("isLogged", isLogged);
        model.addAttribute("cartItems", cartItems);

        if (isLogged) {
            List<AddressResponse> addresses = addressService.getMyAddresses(principal.getName());

            AddressResponse defaultAddress = addresses.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                    .findFirst()
                    .orElse(addresses.isEmpty() ? null : addresses.get(0));

            model.addAttribute("addresses", addresses);
            model.addAttribute("defaultAddress", defaultAddress);
        } else {
            model.addAttribute("addresses", List.of());
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("shippingFee", BigDecimal.ZERO);
        model.addAttribute("finalAmount", totalPrice);

        model.addAttribute("checkoutRequest", new CheckoutRequest());
        model.addAttribute("vouchers", voucherService.getAvailableVouchers());

        return "order/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@ModelAttribute CheckoutRequest request,
                             Principal principal,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        try {
            Long orderId;

            if (principal != null) {
                orderId = orderService.placeOrder(principal.getName(), request);
            } else {
                orderId = orderService.placeGuestOrder(session.getId(), request);
            }

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
        boolean isLogged = principal != null;

        OrderResponse order = isLogged
                ? orderService.getOrderDetail(id, principal.getName())
                : orderService.getGuestOrderDetail(id);

        model.addAttribute("order", order);
        model.addAttribute("isLogged", isLogged);

        return "order/success";
    }

    @GetMapping("/payment/qr/{id}")
    public String qrPayment(@PathVariable Long id,
                            Model model,
                            Principal principal) {

        OrderResponse order;

        if (principal != null) {
            order = orderService.getOrderDetail(id, principal.getName());
        } else {
            order = orderService.getGuestOrderDetail(id);
        }

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
        try {
            payOSService.syncPaymentStatus(orderId);
            redirectAttributes.addFlashAttribute("success", "Thanh toán PayOS thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

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

    @PostMapping("/payment/qr/confirm/{id}")
    public String confirmManualTransfer(@PathVariable Long id,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        try {
            orderService.markManualTransferWaitingConfirm(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu xác nhận thanh toán. Vui lòng chờ admin kiểm tra.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/orders/" + id;
    }


}