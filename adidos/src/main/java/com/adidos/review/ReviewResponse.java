package com.adidos.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String userName;
    private String avatarUrl;
    private Integer rating;
    private String comment;
    private String variantInfo; // VD: Đen / 42
    private LocalDateTime createdAt;
    private List<ReviewResponse> replies; // Dành cho Admin phản hồi
}