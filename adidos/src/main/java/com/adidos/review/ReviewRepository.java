package com.adidos.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndParentIsNullOrderByCreatedAtDesc(Long productId);

    boolean existsByOrderItemId(Long orderItemId);

    List<Review> findByParentIsNullOrderByCreatedAtDesc();

    @Modifying
    @Query("""
        UPDATE Review r
        SET r.orderItem = null
        WHERE r.orderItem.id IN (
            SELECT oi.id FROM OrderItem oi WHERE oi.order.id IN :orderIds
        )
        """)
    void detachOrderItemsByOrderIds(@Param("orderIds") List<Long> orderIds);
}