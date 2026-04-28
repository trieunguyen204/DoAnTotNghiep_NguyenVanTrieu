package com.adidos.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId; // Truyền ra để UI có thể tạo link click vào xem lại sp
    private String productName;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal; // Thành tiền của món này
    private Boolean reviewed;
}