package com.adidos.product.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class VariantDetailResponse {
    private Long id;
    private Long productId;
    private String sizeName;
    private String colorName;
    private List<ImageResponse> imageUrlsWithData;
}