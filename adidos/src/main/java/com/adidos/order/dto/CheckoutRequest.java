package com.adidos.order.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    private Long addressId;
    private String paymentMethod;
    private String voucherCode;
}