package com.adidos.dashboard.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSalesStats {
    private Long productId;
    private String productName;
    private Long soldQuantity;
    private Long stockQuantity;
}