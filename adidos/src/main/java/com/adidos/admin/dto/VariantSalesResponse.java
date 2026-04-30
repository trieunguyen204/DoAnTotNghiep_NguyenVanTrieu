package com.adidos.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantSalesResponse {
    private Long variantId;
    private String colorName;
    private String sizeName;
    private Integer stockQuantity;
    private Long soldQuantity;
}