package com.adidos.order.archive.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivedPaymentDto {
    private Long oldPaymentId;
    private String paymentMethod;
    private String transactionCode;
    private BigDecimal amount;
    private String status;
    private String checkoutUrl;
}