package com.adidos.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VariantRequest {
    private Long id;
    private Long productId;
    private Long sizeId;
    private Long colorId;
    private BigDecimal price;
    private Integer stockQuantity;
}