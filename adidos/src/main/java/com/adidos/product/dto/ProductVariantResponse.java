package com.adidos.product.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariantResponse {
    private Long id;
    private String sizeName;
    private String colorName;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<String> imageUrls;
}