package com.adidos.promotion.service;

import com.adidos.product.entity.Category;
import com.adidos.product.repository.CategoryRepository;
import com.adidos.promotion.entity.Promotion;
import com.adidos.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    /**
     * Tính giá sau khuyến mãi cho một sản phẩm dựa trên danh mục của nó
     */
    // 1. Hàm tìm khuyến mãi tốt nhất (dò lên cả danh mục cha)
    public Promotion getBestPromotionForCategory(Long categoryId) {
        if (categoryId == null) return null;

        Category currentCategory = categoryRepository.findById(categoryId).orElse(null);
        List<Long> categoryIdsToCheck = new ArrayList<>();

        while (currentCategory != null) {
            categoryIdsToCheck.add(currentCategory.getId());
            currentCategory = currentCategory.getParent();
        }

        Promotion bestPromotion = null;

        for (Long catId : categoryIdsToCheck) {
            List<Promotion> activePromotions = promotionRepository.findActivePromotionsByCategory(catId, LocalDateTime.now());

            if (!activePromotions.isEmpty()) {
                Promotion p = activePromotions.get(0);
                if (bestPromotion == null || p.getPriority() > bestPromotion.getPriority()) {
                    bestPromotion = p;
                }
            }
        }

        return bestPromotion;
    }

    // 2. Hàm tính giá gọi lại hàm trên để lấy Best Promotion
    public BigDecimal calculateDiscountedPrice(Long categoryId, BigDecimal originalPrice) {
        if (categoryId == null || originalPrice == null) return originalPrice;

        // Gọi hàm trên để lấy khuyến mãi
        Promotion bestPromotion = getBestPromotionForCategory(categoryId);

        if (bestPromotion == null) return originalPrice;

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(bestPromotion.getDiscountType())) {
            discount = originalPrice.multiply(bestPromotion.getDiscountValue().divide(new BigDecimal(100)));
            if (bestPromotion.getMaxDiscountValue() != null && discount.compareTo(bestPromotion.getMaxDiscountValue()) > 0) {
                discount = bestPromotion.getMaxDiscountValue();
            }
        } else {
            discount = bestPromotion.getDiscountValue();
        }

        BigDecimal finalPrice = originalPrice.subtract(discount);
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }


    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    @Transactional
    public Promotion save(Promotion promotion) {
        if (promotion.getId() != null) {
            Promotion existing = promotionRepository.findById(promotion.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

            existing.setPromotionName(promotion.getPromotionName());
            existing.setDiscountType(promotion.getDiscountType());
            existing.setDiscountValue(promotion.getDiscountValue());
            existing.setMaxDiscountValue(promotion.getMaxDiscountValue());
            existing.setPriority(promotion.getPriority());
            existing.setStartDate(promotion.getStartDate());
            existing.setEndDate(promotion.getEndDate());
            existing.setStatus(promotion.getStatus());

            if (promotion.getCategories() != null && !promotion.getCategories().isEmpty()) {
                List<Long> catIds = promotion.getCategories().stream().map(Category::getId).toList();


                java.util.Set<Category> managedCategories = new java.util.HashSet<>(categoryRepository.findAllById(catIds));

                existing.setCategories(managedCategories);
            } else {
                existing.getCategories().clear();
            }

            return promotionRepository.save(existing);
        } else {
            if (promotion.getCategories() != null && !promotion.getCategories().isEmpty()) {
                List<Long> catIds = promotion.getCategories().stream().map(Category::getId).toList();


                java.util.Set<Category> managedCategories = new java.util.HashSet<>(categoryRepository.findAllById(catIds));

                promotion.setCategories(managedCategories);
            }
            return promotionRepository.save(promotion);
        }
    }

    public void deleteById(Long id) {
        promotionRepository.deleteById(id);
    }
}