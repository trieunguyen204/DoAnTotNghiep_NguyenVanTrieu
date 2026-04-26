package com.adidos.cart.mapper;

import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.entity.CartItem;
import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;
import com.adidos.promotion.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

public class CartMapper {

    public static CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) return null;

        ProductVariant variant = cartItem.getProductVariant();

        // 1. Lấy ảnh primary (hoặc ảnh đầu tiên)
        String imageUrl = variant.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(variant.getImages().isEmpty() ? null : variant.getImages().get(0).getImageUrl());

        // 2. Lấy giá gốc
        BigDecimal originalPrice = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
        BigDecimal finalPrice = originalPrice;

        // 3. TÍNH GIÁ SAU KHUYẾN MÃI (Nếu có)
        if (variant.getProduct().getCategory() != null && variant.getProduct().getCategory().getPromotions() != null) {
            LocalDateTime now = LocalDateTime.now();

            // Lọc ra các KM đang Active, trong thời gian áp dụng và có độ ưu tiên cao nhất
            Promotion activePromo = variant.getProduct().getCategory().getPromotions().stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus())
                            && p.getStartDate() != null && !now.isBefore(p.getStartDate())
                            && p.getEndDate() != null && !now.isAfter(p.getEndDate()))
                    .max(Comparator.comparing(p -> p.getPriority() != null ? p.getPriority() : 0))
                    .orElse(null);

            if (activePromo != null) {
                if ("PERCENT".equals(activePromo.getDiscountType())) {
                    // Giảm theo phần trăm
                    BigDecimal discountAmt = originalPrice.multiply(activePromo.getDiscountValue()).divide(new BigDecimal(100));

                    // Giới hạn số tiền giảm tối đa (nếu có cấu hình)
                    if (activePromo.getMaxDiscountValue() != null && discountAmt.compareTo(activePromo.getMaxDiscountValue()) > 0) {
                        discountAmt = activePromo.getMaxDiscountValue();
                    }
                    finalPrice = originalPrice.subtract(discountAmt);
                } else if ("FIXED".equals(activePromo.getDiscountType())) {
                    // Giảm thẳng tiền mặt
                    finalPrice = originalPrice.subtract(activePromo.getDiscountValue());
                }

                // Đảm bảo giá không bao giờ bị âm
                if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    finalPrice = BigDecimal.ZERO;
                }
            }
        }

        // 4. Tính tổng tiền dựa trên GIÁ ĐÃ GIẢM
        BigDecimal quantity = new BigDecimal(cartItem.getQuantity());
        BigDecimal subTotal = finalPrice.multiply(quantity);

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productName(variant.getProduct().getName())
                .sizeName(variant.getSize().getSizeName())
                .colorName(variant.getColor().getColorName())
                .imageUrl(imageUrl)
                .originalPrice(originalPrice)
                .price(finalPrice)
                .quantity(cartItem.getQuantity())
                .subTotal(subTotal)
                .build();
    }
}