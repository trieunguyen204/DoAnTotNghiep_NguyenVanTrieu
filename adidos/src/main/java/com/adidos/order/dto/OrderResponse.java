package com.adidos.order.dto;

import com.adidos.order.enums.OrderStatus;
import com.adidos.order.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private String receiverName;
    private String shippingAddress;

    // Các loại tiền
    private BigDecimal totalPrice; // Tổng tiền hàng
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount; // Thực tế khách phải trả = total + ship - discount

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;

    private String receiverPhone;

    private List<OrderItemResponse> items;
    private String paymentMethod;
}