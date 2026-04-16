package com.adidos.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageResponse {
    private Long id;
    private String url;
    private Boolean isPrimary;
}