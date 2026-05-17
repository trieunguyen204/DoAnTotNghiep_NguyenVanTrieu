package com.adidos.tryon.service;

import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TryOnImageResolverService {

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public String resolveGarmentImageUrl(Long productId, ProductVariant variant) {
        if (variant == null || variant.getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn màu/kích cỡ trước khi thử đồ");
        }

        if (variant.getProduct() == null || !variant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Biến thể không thuộc sản phẩm hiện tại");
        }

        if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())) {
            throw new IllegalArgumentException("Biến thể này hiện không hoạt động");
        }

        List<ProductImage> images = variant.getImages();
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Biến thể đang chọn chưa có ảnh để thử đồ");
        }

        ProductImage selected = images.stream()
                .filter(img -> hasText(img.getImageUrl()))
                .sorted(Comparator
                        .comparing((ProductImage img) -> !isTryOnImage(img))
                        .thenComparing(img -> !Boolean.TRUE.equals(img.getIsPrimary()))
                        .thenComparing(img -> img.getSortOrder() == null ? Integer.MAX_VALUE : img.getSortOrder())
                        .thenComparing(img -> img.getId() == null ? Long.MAX_VALUE : img.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Biến thể đang chọn chưa có ảnh hợp lệ"));

        return Paths.get(
                System.getProperty("user.dir"),
                selected.getImageUrl()
                ).toAbsolutePath().toString();
    }

    private boolean isTryOnImage(ProductImage image) {
        try {
            Object value = image.getClass().getMethod("getImageType").invoke(image);
            return value != null && "TRY_ON".equalsIgnoreCase(value.toString());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    public String toPublicUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return imageUrl;
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
            return imageUrl;
        }

        String base = appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length() - 1) : appBaseUrl;
        return base + "/uploads/" + imageUrl;
    }
}
