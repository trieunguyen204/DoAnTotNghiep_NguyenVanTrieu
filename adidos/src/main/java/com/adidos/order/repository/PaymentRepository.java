package com.adidos.order.repository;

import com.adidos.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionCode(String transactionCode);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.order.id IN :orderIds")
    void deleteByOrderIds(@Param("orderIds") List<Long> orderIds);
}