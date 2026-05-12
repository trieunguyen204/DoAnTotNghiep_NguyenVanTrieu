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

    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Dùng khi form gửi sẵn full address
    private String guestAddress;

    // Dùng cho checkout.html hiện tại
    private String guestProvince;
    private String guestDistrict;
    private String guestWard;
    private String guestAddressDetail;

    public String buildGuestFullAddress() {
        if (guestAddress != null && !guestAddress.isBlank()) {
            return guestAddress.trim();
        }

        String detail = guestAddressDetail == null ? "" : guestAddressDetail.trim();
        String ward = guestWard == null ? "" : guestWard.trim();
        String district = guestDistrict == null ? "" : guestDistrict.trim();
        String province = guestProvince == null ? "" : guestProvince.trim();

        String full = String.join(", ", detail, ward, district, province)
                .replaceAll("(,\\s*)+", ", ")
                .replaceAll("^,\\s*|,\\s*$", "");

        return full.isBlank() ? null : full;
    }
}
