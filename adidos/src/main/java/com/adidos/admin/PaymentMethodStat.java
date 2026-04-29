package com.adidos.admin;

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