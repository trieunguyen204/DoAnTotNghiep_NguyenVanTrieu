package com.adidos.user.dto;
import lombok.Data;

@Data
public class AddressRequest {
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private Boolean isDefault;
}