package com.adidos.order.service;

import com.adidos.order.entity.Order;
import com.adidos.order.entity.Payment;
import com.adidos.order.enums.PaymentStatus;
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
                            .status(PaymentStatus.UNPAID)
                            .build());

            payment.setPaymentMethod("PAYOS");
            payment.setTransactionCode(String.valueOf(orderCode));
            payment.setAmount(finalAmount);
            payment.setStatus(PaymentStatus.UNPAID);
            payment.setCheckoutUrl(paymentLink.getCheckoutUrl());

            paymentRepository.save(payment);
            orderRepository.save(order);

            return paymentLink.getCheckoutUrl();

        } catch (Exception e) {
            orderService.markPaymentFailedAndCancel(orderId);
            throw new RuntimeException("Lỗi tạo link thanh toán PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(Map<String, Object> body) {
        Map<String, Object> data = (Map<String, Object>) body.get("data");

        if (data == null || data.get("orderCode") == null) {
            return;
        }

        Long orderCode = ((Number) data.get("orderCode")).longValue();

        Payment payment = paymentRepository.findByTransactionCode(String.valueOf(orderCode))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

        String code = String.valueOf(body.get("code"));

        if ("00".equals(code)) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            orderService.markPayOSPaid(payment.getOrder().getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            orderService.markPaymentFailedAndCancel(payment.getOrder().getId());
        }
    }

    @Transactional
    public void syncPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

        try {
            Long orderCode = Long.valueOf(payment.getTransactionCode());
            var info = payOS.paymentRequests().get(orderCode);

            String status = info.getStatus().name();

            if ("PAID".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);

                orderService.markPayOSPaid(orderId);

            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                orderService.markPaymentFailedAndCancel(orderId);

            } else {
                payment.setStatus(PaymentStatus.UNPAID);
                paymentRepository.save(payment);

                throw new RuntimeException("PayOS chưa xác nhận thanh toán. Trạng thái hiện tại: " + status);
            }

        } catch (NumberFormatException e) {
            throw new RuntimeException("Mã giao dịch PayOS không hợp lệ");
        } catch (Exception e) {
            throw new RuntimeException("Không thể kiểm tra trạng thái PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelPayOSOrder(Long orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });

        orderService.markPaymentFailedAndCancel(orderId);
    }
}
