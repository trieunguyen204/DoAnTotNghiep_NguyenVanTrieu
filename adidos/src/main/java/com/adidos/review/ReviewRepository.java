package com.adidos.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy comment gốc (không phải reply) của 1 sản phẩm
    List<Review> findByProductIdAndParentIsNullOrderByCreatedAtDesc(Long productId);
}