package com.adidos.order.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutRequest {

    private String receiverName;
    private String phone;
    private String shippingAddress;

    // Phương thức thanh toán (COD hoặc VNPAY...)
    private String paymentMethod;

    private String voucherCode;
}