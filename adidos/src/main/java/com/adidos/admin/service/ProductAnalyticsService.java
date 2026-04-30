package com.adidos.admin.service;


import com.adidos.admin.dto.ProductAnalyticsResponse;
import com.adidos.admin.dto.VariantSalesResponse;
import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.repository.OrderRepository;
import com.adidos.product.entity.Product;
import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductRepository;
import com.adidos.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/*
Bán chạy = có lượt bán > 3, tính từ đơn DELIVERED, sắp xếp giảm dần
Bán ế = không thuộc top bán chạy và bán <= 3
Sắp hết hàng = tồn kho <= 5
*/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductAnalyticsService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;

    public List<ProductAnalyticsResponse> getProductsByType(String type) {
        List<Product> products = productRepository.findAll();
        Map<Long, Long> soldMap = buildSoldMap();

        List<ProductAnalyticsResponse> result = products.stream()
                .map(product -> {
                    Integer totalStock = product.getVariants() == null ? 0 :
                            product.getVariants().stream()
                                    .map(ProductVariant::getStockQuantity)
                                    .filter(Objects::nonNull)
                                    .reduce(0, Integer::sum);

                    Long totalSold = soldMap.getOrDefault(product.getId(), 0L);

                    return ProductAnalyticsResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .imageUrl(getPrimaryImage(product))
                            .totalStock(totalStock)
                            .totalSold(totalSold)
                            .build();
                })
                .collect(Collectors.toList());

        List<ProductAnalyticsResponse> soldProducts = result.stream()
                .filter(p -> p.getTotalSold() != null && p.getTotalSold() > 3)
                .sorted(Comparator.comparing(ProductAnalyticsResponse::getTotalSold).reversed())
                .collect(Collectors.toList());

        List<Long> bestSellingIds = soldProducts.stream()
                .limit(10)
                .map(ProductAnalyticsResponse::getProductId)
                .toList();

        if ("low-stock".equalsIgnoreCase(type)) {
            return result.stream()
                    .filter(p -> p.getTotalStock() != null && p.getTotalStock() <= 5)
                    .sorted(Comparator.comparing(ProductAnalyticsResponse::getTotalStock))
                    .collect(Collectors.toList());
        }

        if ("slow-selling".equalsIgnoreCase(type)) {
            return result.stream()
                    .filter(p -> !bestSellingIds.contains(p.getProductId()))
                    .filter(p -> p.getTotalSold() != null && p.getTotalSold() >= 0 && p.getTotalSold() <= 3)
                    .sorted(Comparator.comparing(ProductAnalyticsResponse::getTotalSold))
                    .collect(Collectors.toList());
        }


        return soldProducts.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<VariantSalesResponse> getVariantSales(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        Map<Long, Long> variantSoldMap = buildVariantSoldMap();

        return variants.stream()
                .map(v -> VariantSalesResponse.builder()
                        .variantId(v.getId())
                        .colorName(v.getColor() != null ? v.getColor().getColorName() : "N/A")
                        .sizeName(v.getSize() != null ? v.getSize().getSizeName() : "N/A")
                        .stockQuantity(v.getStockQuantity())
                        .soldQuantity(variantSoldMap.getOrDefault(v.getId(), 0L))
                        .build())
                .sorted(Comparator.comparing(VariantSalesResponse::getSoldQuantity).reversed())
                .collect(Collectors.toList());
    }

    private Map<Long, Long> buildSoldMap() {
        Map<Long, Long> map = new HashMap<>();

        List<Order> deliveredOrders = orderRepository.findByOrderStatus(OrderStatus.DELIVERED);

        for (Order order : deliveredOrders) {
            if (order.getOrderItems() == null) continue;

            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductVariant() == null ||
                        item.getProductVariant().getProduct() == null) {
                    continue;
                }

                Long productId = item.getProductVariant().getProduct().getId();
                Long qty = item.getQuantity() == null ? 0L : item.getQuantity().longValue();

                map.put(productId, map.getOrDefault(productId, 0L) + qty);
            }
        }

        return map;
    }

    private Map<Long, Long> buildVariantSoldMap() {
        Map<Long, Long> map = new HashMap<>();

        List<Order> deliveredOrders = orderRepository.findByOrderStatus(OrderStatus.DELIVERED);

        for (Order order : deliveredOrders) {
            if (order.getOrderItems() == null) continue;

            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductVariant() == null) continue;

                Long variantId = item.getProductVariant().getId();
                Long qty = item.getQuantity() == null ? 0L : item.getQuantity().longValue();

                map.put(variantId, map.getOrDefault(variantId, 0L) + qty);
            }
        }

        return map;
    }

    private String getPrimaryImage(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return "/images/no-image.png";
        }

        for (ProductVariant variant : product.getVariants()) {
            if (variant.getImages() == null || variant.getImages().isEmpty()) {
                continue;
            }

            String imageUrl = variant.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(variant.getImages().get(0).getImageUrl());

            return normalizeImageUrl(imageUrl);
        }

        return "/images/no-image.png";
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "/images/no-image.png";
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        if (imageUrl.startsWith("/uploads/")) {
            return imageUrl;
        }

        if (imageUrl.startsWith("uploads/")) {
            return "/" + imageUrl;
        }

        if (imageUrl.startsWith("/")) {
            return imageUrl;
        }

        return "/uploads/" + imageUrl;
    }
}