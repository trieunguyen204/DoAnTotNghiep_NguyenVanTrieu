package com.adidos.cart.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemRequest {
    private Long productVariantId;
    private Integer quantity;
}