package com.adidos.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAnalyticsResponse {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer totalStock;
    private Long totalSold;
}