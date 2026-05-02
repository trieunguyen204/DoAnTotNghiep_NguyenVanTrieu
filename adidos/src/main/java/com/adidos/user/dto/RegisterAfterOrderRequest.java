package com.adidos.user.dto;

import lombok.Data;

@Data
public class RegisterAfterOrderRequest {
    private Long orderId;
    private String email;
    private String password;
    private String confirmPassword;
    private String fullName;
    private String phone;
}