package com.adidos.review.mapper;

import com.adidos.review.dto.ReviewResponse;
import com.adidos.review.entity.Review;
import java.util.stream.Collectors;

public class ReviewMapper {
    public static ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        String variantInfo = review.getProductVariant() != null ?
                review.getProductVariant().getColor().getColorName() + " / " + review.getProductVariant().getSize().getSizeName() : null;

        return ReviewResponse.builder()
                .id(review.getId())
                .userName(review.getUser().getFullName())
                .avatarUrl(review.getUser().getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .variantInfo(variantInfo)
                .createdAt(review.getCreatedAt())
                .replies(review.getReplies() != null ?
                        review.getReplies().stream().map(ReviewMapper::toResponse).collect(Collectors.toList()) : null)
                .build();
    }
}