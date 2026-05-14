package com.adidos.order.repository;

import com.adidos.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        select oi.productVariant.product.id, coalesce(sum(oi.quantity), 0)
        from OrderItem oi
        where oi.order.orderStatus <> com.adidos.order.enums.OrderStatus.CANCELLED
        group by oi.productVariant.product.id
    """)
    List<Object[]> findBestSellerProductQuantity();


    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    void deleteByOrderIds(@Param("orderIds") List<Long> orderIds);
}