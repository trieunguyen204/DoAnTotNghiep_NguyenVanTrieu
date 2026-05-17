package com.adidos.tryon.dto;

import com.adidos.tryon.entity.TryOnStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TryOnResponse {

    private Long id;

    private Long productId;

    private Long variantId;

    private String colorName;

    private String sizeName;

    private String personImageUrl;

    private String garmentImageUrl;

    private String resultImageUrl;

    private TryOnStatus status;

    private String errorMessage;

    private List<TryOnColorResult> colorResults;
}