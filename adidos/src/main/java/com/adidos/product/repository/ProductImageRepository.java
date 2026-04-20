package com.adidos.product.repository;

import com.adidos.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Lấy tất cả ảnh của một biến thể
    List<ProductImage> findByProductVariantIdOrderBySortOrderAsc(Long variantId);

    // Lấy ảnh chính (primary) của một biến thể
    Optional<ProductImage> findByProductVariantIdAndIsPrimaryTrue(Long variantId);

    long countByProductVariantId(Long variantId);
}