package com.adidos.admin.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodStat {
    private String paymentMethod;
    private Long total;
}