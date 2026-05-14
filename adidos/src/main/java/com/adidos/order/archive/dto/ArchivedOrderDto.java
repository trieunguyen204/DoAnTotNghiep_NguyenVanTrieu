package com.adidos.order.archive.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivedOrderDto {
    private Long oldOrderId;
    private Long orderCode;

    private Long oldUserId;
    private String userEmail;
    private String userFullName;
    private String userPhone;

    private Long oldVoucherId;
    private String voucherCode;

    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String guestAddress;

    private BigDecimal totalPrice;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;

    private String orderStatus;
    private LocalDateTime createdAt;

    private ArchivedPaymentDto payment;
    private List<ArchivedOrderItemDto> items;
}