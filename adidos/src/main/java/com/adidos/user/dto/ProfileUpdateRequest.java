package com.adidos.user.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private String avatarUrl;
    private MultipartFile avatarFile;

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}