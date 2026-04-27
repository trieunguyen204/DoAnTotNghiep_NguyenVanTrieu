package com.adidos.order.mapper;

import com.adidos.order.dto.OrderItemResponse;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        if (order == null) return null;

        // Xử lý null-safe cho các khoản tiền
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal ship = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;

        // Tính tổng thực thu: Tiền hàng + Phí ship - Giảm giá
        BigDecimal finalAmount = total.add(ship).subtract(discount);

        return OrderResponse.builder()
                .id(order.getId())
                .receiverName(order.getReceiverName())
                .shippingAddress(order.getShippingAddress())
                .totalPrice(total)
                .receiverPhone(order.getReceiverPhone())
                .shippingFee(ship)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(toItemResponseList(order.getOrderItems()))
                .build();
    }

    private static List<OrderItemResponse> toItemResponseList(List<OrderItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;

        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        BigDecimal qty = new BigDecimal(item.getQuantity() != null ? item.getQuantity() : 0);

        return OrderItemResponse.builder()
                .id(item.getId())
                .variantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                .productName(item.getProductName())
                .color(item.getColor())
                .size(item.getSize())
                .price(price)
                .quantity(item.getQuantity())
                .subTotal(price.multiply(qty)) // Tự động tính thành tiền
                .build();
    }
}