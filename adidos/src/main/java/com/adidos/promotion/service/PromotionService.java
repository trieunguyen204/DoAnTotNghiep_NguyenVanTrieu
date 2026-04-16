package com.adidos.promotion.service;

import com.adidos.promotion.entity.Promotion;
import com.adidos.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    /**
     * Tính giá sau khuyến mãi cho một sản phẩm dựa trên danh mục của nó
     */
    public BigDecimal calculateDiscountedPrice(Long categoryId, BigDecimal originalPrice) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotionsByCategory(categoryId, LocalDateTime.now());

        if (activePromotions.isEmpty()) return originalPrice;

        // Lấy khuyến mãi có độ ưu tiên cao nhất
        Promotion p = activePromotions.get(0);
        BigDecimal discount = BigDecimal.ZERO;

        if ("PERCENT".equalsIgnoreCase(p.getDiscountType())) {
            discount = originalPrice.multiply(p.getDiscountValue().divide(new BigDecimal(100)));
            // Kiểm tra mức giảm tối đa (max_discount_value)
            if (p.getMaxDiscountValue() != null && discount.compareTo(p.getMaxDiscountValue()) > 0) {
                discount = p.getMaxDiscountValue();
            }
        } else {
            discount = p.getDiscountValue();
        }

        BigDecimal finalPrice = originalPrice.subtract(discount);
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}