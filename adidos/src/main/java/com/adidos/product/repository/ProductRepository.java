package com.adidos.product.repository;

import com.adidos.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Lấy danh sách sản phẩm đang hoạt động (ACTIVE)
    List<Product> findByStatus(String status);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Long id);

    // Đếm tổng số Sản phẩm
    long count();

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:kw% OR p.brand LIKE %:kw%")
    List<Product> searchProducts(@Param("kw") String keyword);

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.category.id = :id OR p.category.parent.id = :id OR p.category.id = (SELECT c.parent.id FROM Category c WHERE c.id = :id)")
    List<Product> findProductsByCategoryAndSub(@Param("id") Long id);

    Optional<Product> findByName(String name);

}