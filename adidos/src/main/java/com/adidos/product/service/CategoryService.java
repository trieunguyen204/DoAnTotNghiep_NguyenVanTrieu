package com.adidos.product.service;

import com.adidos.product.dto.CategoryResponse;
import com.adidos.product.entity.Category;
import com.adidos.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Lấy danh mục gốc (để làm Menu cha)
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy danh mục con (khi hover vào menu cha)
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    private CategoryResponse toResponse(Category category) {
        // Map danh sách danh mục con (nếu có)
        List<CategoryResponse> subs = category.getSubCategories() != null ?
                category.getSubCategories().stream()
                        .map(sub -> CategoryResponse.builder()
                                .id(sub.getId())
                                .name(sub.getName())
                                .parentId(category.getId())
                                .build())
                        .collect(Collectors.toList()) : null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .subCategories(subs) // TRUYỀN VÀO ĐÂY
                .build();
    }


    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        return toResponse(category);
    }
}