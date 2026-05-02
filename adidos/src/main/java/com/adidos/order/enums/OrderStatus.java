package com.adidos.order.enums;

public enum OrderStatus {
    PENDING,        // Chờ xác nhận
    PROCESSING,     // Đang chuẩn bị hàng
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao thành công
    CANCELLED       // Đã hủy
    ,WAITING_PAYMENT

}