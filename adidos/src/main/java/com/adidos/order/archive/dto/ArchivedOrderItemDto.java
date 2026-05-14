package com.adidos.order.archive.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivedOrderItemDto {
    private Long oldOrderItemId;
    private Long oldProductVariantId;
    private Long oldProductId;

    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String color;
    private String size;
}