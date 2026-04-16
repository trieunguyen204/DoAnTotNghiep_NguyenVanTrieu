package com.adidos.user.dto;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String phone;
    private String fullAddress; // Ghép: addressDetail, ward, district, province
    private Boolean isDefault;
}