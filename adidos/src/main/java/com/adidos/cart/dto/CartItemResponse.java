package com.adidos.cart.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private Long productId;

    // Thông tin hiển thị
    private String productName;
    private String sizeName;
    private String colorName;
    private String imageUrl;

    // Thông tin tính toán
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
    private Integer maxStock;
}