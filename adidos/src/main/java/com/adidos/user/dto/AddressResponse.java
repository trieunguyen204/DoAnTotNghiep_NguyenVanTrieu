package com.adidos.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;

    private String receiverName;
    private String phone;

    private String province;
    private String district;
    private String ward;
    private String addressDetail;

    private String fullAddress;
    private Boolean isDefault;
}