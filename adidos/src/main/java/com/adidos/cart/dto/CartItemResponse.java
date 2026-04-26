package com.adidos.cart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private Long productId;
    private String productName;
    private String sizeName;
    private String colorName;
    private String imageUrl;

    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer quantity;
    private BigDecimal subTotal;
    private Integer maxStock;
}