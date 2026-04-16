package com.adidos.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_method")
    private String paymentMethod; // VD: COD, VNPAY, MOMO

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    private String status; // SUCCESS, FAILED, PENDING
}