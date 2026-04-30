package com.adidos.order.repository;

import com.adidos.order.entity.Order;
import com.adidos.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.id DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    Page<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);


    // Đếm tổng số đơn hàng
    long count();

    // Đếm đơn hàng theo trạng thái (VD: Để biết có bao nhiêu đơn đang PENDING cần xử lý)
    long countByOrderStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();
}