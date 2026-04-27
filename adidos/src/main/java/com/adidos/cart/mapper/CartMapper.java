package com.adidos.cart.mapper;

import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.entity.CartItem;
import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;

import java.math.BigDecimal;

public class CartMapper {

    public static CartItemResponse toResponse(CartItem cartItem, BigDecimal finalPrice) {
        if (cartItem == null) return null;

        ProductVariant variant = cartItem.getProductVariant();

        String imageUrl = variant.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(variant.getImages().isEmpty() ? null : variant.getImages().get(0).getImageUrl());

        BigDecimal originalPrice = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
        BigDecimal price = finalPrice != null ? finalPrice : originalPrice;
        BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
        BigDecimal subTotal = price.multiply(quantity);

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .variantId(variant.getId())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .sizeName(variant.getSize().getSizeName())
                .colorName(variant.getColor().getColorName())
                .imageUrl(imageUrl)
                .originalPrice(originalPrice)
                .price(price)
                .quantity(cartItem.getQuantity())
                .subTotal(subTotal)
                .maxStock(variant.getStockQuantity())
                .build();
    }
}