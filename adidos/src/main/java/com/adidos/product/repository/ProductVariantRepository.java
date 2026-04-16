package com.adidos.product.repository;

import com.adidos.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Lấy tất cả các biến thể của một sản phẩm
    List<ProductVariant> findByProductId(Long productId);

    // Kiểm tra xem một biến thể cụ thể (Product + Size + Color) đã tồn tại chưa
    Optional<ProductVariant> findByProductIdAndSizeIdAndColorId(Long productId, Long sizeId, Long colorId);
}