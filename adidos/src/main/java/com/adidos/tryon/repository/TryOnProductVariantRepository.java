package com.adidos.tryon.repository;

import com.adidos.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TryOnProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "size", "color", "images"})
    Optional<ProductVariant> findById(Long id);

    @EntityGraph(attributePaths = {"product", "size", "color", "images"})
    List<ProductVariant> findAllByProductId(Long productId);
}