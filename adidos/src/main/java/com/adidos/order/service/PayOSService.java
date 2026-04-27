package com.adidos.order.service;

import com.adidos.order.entity.Order;
import com.adidos.order.entity.Payment;
import com.adidos.order.repository.OrderRepository;
import com.adidos.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public String createPaymentLink(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        BigDecimal finalAmount = order.getTotalPrice()
                .add(order.getShippingFee())
                .subtract(order.getDiscountAmount());

        Long orderCode = order.getOrderCode();

        if (orderCode == null) {
            orderCode = System.currentTimeMillis();
            order.setOrderCode(orderCode);
            orderRepository.save(order);
        }

        try {
            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(finalAmount.longValue())
                    .description("ADIDOS DH" + order.getId())
                    .returnUrl(baseUrl + "/payment/payos/return?orderId=" + order.getId())
                    .cancelUrl(baseUrl + "/payment/payos/cancel?orderId=" + order.getId())
                    .build();

            var paymentLink = payOS.paymentRequests().create(request);

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseGet(() -> Payment.builder()
                            .order(order)
                            .paymentMethod("PAYOS")
                            .amount(finalAmount)
                            .status("PENDING")
                            .build());

            payment.setPaymentMethod("PAYOS");
            payment.setTransactionCode(String.valueOf(orderCode));
            payment.setAmount(finalAmount);
            payment.setStatus("PENDING");
            payment.setCheckoutUrl(paymentLink.getCheckoutUrl());

            paymentRepository.save(payment);

            return paymentLink.getCheckoutUrl();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link thanh toán PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(Map<String, Object> body) {
        Map<String, Object> data = (Map<String, Object>) body.get("data");

        if (data == null) {
            return;
        }

        String code = String.valueOf(body.get("code"));
        Long orderCode = ((Number) data.get("orderCode")).longValue();

        Payment payment = paymentRepository.findByTransactionCode(String.valueOf(orderCode))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

        Order order = payment.getOrder();

        if ("00".equals(code)) {
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);

            orderService.markPayOSPaid(order.getId());

        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);

            orderService.markPaymentFailedAndCancel(order.getId());
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }


}