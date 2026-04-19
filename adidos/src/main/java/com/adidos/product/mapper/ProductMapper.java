package com.adidos.product.mapper;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.dto.ProductVariantResponse;
import com.adidos.product.entity.Product;
import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponse toProductResponse(Product product) {
        if (product == null) return null;

        List<ProductVariant> variants = product.getVariants();

        // Tính giá gốc (lấy giá của biến thể rẻ nhất)
        BigDecimal minPrice = (variants != null && !variants.isEmpty()) ?
                variants.stream()
                        .map(ProductVariant::getPrice)
                        .filter(p -> p != null)
                        .min(Comparator.naturalOrder())
                        .orElse(BigDecimal.ZERO) : BigDecimal.ZERO;

        // Lấy ảnh đại diện
        String primaryImage = (variants != null && !variants.isEmpty()) ?
                variants.stream()
                        .flatMap(v -> v.getImages().stream())
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(null) : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .material(product.getMaterial())
                .gender(product.getGender())
                .status(product.getStatus())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .description(product.getDescription())
                .brand(product.getBrand())
                .categoryName(product.getCategory() != null ?
                        (product.getCategory().getParent() != null ? product.getCategory().getParent().getName() + " -> " : "") + product.getCategory().getName()
                        : null)
                .originalPrice(minPrice)
                .primaryImageUrl(primaryImage)
                .variants(toVariantResponseList(variants))
                .build();
    }

    private static List<ProductVariantResponse> toVariantResponseList(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream()
                .map(ProductMapper::toVariantResponse)
                .collect(Collectors.toList());
    }

    private static ProductVariantResponse toVariantResponse(ProductVariant variant) {
        if (variant == null) return null;


        List<String> images = variant.getImages() != null ?
                variant.getImages().stream()
                        .sorted((img1, img2) -> {
                            boolean isP1 = Boolean.TRUE.equals(img1.getIsPrimary());
                            boolean isP2 = Boolean.TRUE.equals(img2.getIsPrimary());
                            return Boolean.compare(isP2, isP1); // Đưa true lên trước false
                        })
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .sizeId(variant.getSize() != null ? variant.getSize().getId() : null)
                .sizeName(variant.getSize() != null ? variant.getSize().getSizeName() : null)
                .colorId(variant.getColor() != null ? variant.getColor().getId() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getColorName() : null)
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrls(images)
                .build();
    }
}