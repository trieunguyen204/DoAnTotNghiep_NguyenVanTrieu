package com.adidos.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndParentIsNullOrderByCreatedAtDesc(Long productId);

    boolean existsByOrderItemId(Long orderItemId);

    List<Review> findByParentIsNullOrderByCreatedAtDesc();
}