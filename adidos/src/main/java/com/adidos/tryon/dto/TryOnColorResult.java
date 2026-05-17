package com.adidos.tryon.dto;

import com.adidos.tryon.entity.TryOnStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TryOnColorResult {

    private Long variantId;

    private String colorName;

    private String sizeName;

    private String garmentImageUrl;

    private String resultImageUrl;

    private TryOnStatus status;

    private String errorMessage;
}