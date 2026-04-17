package com.adidos.product.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> subCategories;
}