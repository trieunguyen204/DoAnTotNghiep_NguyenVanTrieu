package com.adidos.admin.entity;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockProduct {
    private String productName;
    private String variantInfo;
    private Integer stockQuantity;
}