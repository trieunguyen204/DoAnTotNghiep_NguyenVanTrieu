package com.adidos.product.repository;

import com.adidos.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT p FROM Product p WHERE p.category.id = :id OR p.category.parent.id = :id OR p.category.id = (SELECT c.parent.id FROM Category c WHERE c.id = :id)")
    List<Product> findProductsByCategoryAndSub(@Param("id") Long id);

    Optional<Product> findByName(String name);

    List<Product> findTop4ByCategoryIdInAndStatus(List<Long> categoryIds, String status);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategoryIdInAndStatus(List<Long> categoryIds, String status, Pageable pageable);


    Page<Product> findAll(Pageable pageable);

    @Query("""
    SELECT 
        p.id,
        p.name,
        COALESCE(SUM(oi.quantity), 0),
        COALESCE(SUM(v.stockQuantity), 0)
    FROM Product p
    LEFT JOIN p.variants v
    LEFT JOIN OrderItem oi 
        ON oi.productVariant = v
        AND oi.order.orderStatus <> com.adidos.order.enums.OrderStatus.CANCELLED
        AND (oi.order.restoredFromArchive = false OR oi.order.restoredFromArchive IS NULL)
        AND oi.order.createdAt BETWEEN :start AND :end
    GROUP BY p.id, p.name
    """)
    List<Object[]> getAllProductSalesStats(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
}