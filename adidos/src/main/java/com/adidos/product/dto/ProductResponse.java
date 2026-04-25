package com.adidos.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String categoryName;
    private String brand;
    private String description;
    private String material;
    private String gender;
    private String status;
    private Long categoryId;


    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private Boolean hasPromotion;
    // ------------------------------------

    private String primaryImageUrl;
    private List<ProductVariantResponse> variants;

    private Boolean isNew;
    private String promotionName;

    private String discountType;
    private java.math.BigDecimal promotionDiscountValue;
}