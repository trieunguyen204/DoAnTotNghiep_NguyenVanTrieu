package com.adidos.user.dto;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
}