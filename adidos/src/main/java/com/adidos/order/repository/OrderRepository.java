package com.adidos.order.repository;

import com.adidos.order.entity.Order;
import com.adidos.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    List<Order> findByUserIdOrderByIdDesc(Long userId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

    Page<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    // Đếm tổng số đơn hàng
    long count();

    long countByOrderStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    List<Order> findByGuestEmailIgnoreCaseOrderByIdDesc(String guestEmail);

    @Query("""
    SELECT DISTINCT o FROM Order o
    LEFT JOIN FETCH o.orderItems i
    LEFT JOIN FETCH i.productVariant v
    LEFT JOIN FETCH v.product p
    LEFT JOIN FETCH v.color
    LEFT JOIN FETCH v.size
    WHERE LOWER(o.guestEmail) = LOWER(:email)
    ORDER BY o.id DESC
    """)
    List<Order> findGuestOrdersWithItemsByEmail(@Param("email") String email);

    @Query("""
    SELECT DISTINCT o FROM Order o
    LEFT JOIN FETCH o.orderItems i
    LEFT JOIN FETCH i.productVariant v
    LEFT JOIN FETCH v.product p
    LEFT JOIN FETCH v.color
    LEFT JOIN FETCH v.size
    WHERE o.user.id = :userId
    ORDER BY o.id DESC
    """)
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("""
    SELECT COALESCE(SUM(o.totalPrice + o.shippingFee - o.discountAmount), 0)
    FROM Order o
    WHERE o.orderStatus = com.adidos.order.enums.OrderStatus.DELIVERED
    """)
    BigDecimal calculateTotalRevenueAllTime();

    @Query("""
    SELECT MONTH(o.createdAt), COALESCE(SUM(o.totalPrice + o.shippingFee - o.discountAmount), 0)
    FROM Order o
    WHERE o.orderStatus = com.adidos.order.enums.OrderStatus.DELIVERED
      AND YEAR(o.createdAt) = :year
    GROUP BY MONTH(o.createdAt)
    ORDER BY MONTH(o.createdAt)
    """)
    List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);


    @Query("""
    SELECT 
        oi.productVariant.product.id,
        oi.productVariant.product.name,
        COALESCE(SUM(oi.quantity), 0),
        COALESCE((
            SELECT SUM(v.stockQuantity)
            FROM ProductVariant v
            WHERE v.product.id = oi.productVariant.product.id
        ), 0)
    FROM OrderItem oi
    WHERE oi.order.orderStatus <> com.adidos.order.enums.OrderStatus.CANCELLED
      AND YEAR(oi.order.createdAt) = :year
    GROUP BY oi.productVariant.product.id, oi.productVariant.product.name
    """)
    List<Object[]> getProductSalesStatsByYear(@Param("year") int year);
}