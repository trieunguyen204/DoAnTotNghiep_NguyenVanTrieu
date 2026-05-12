package com.adidos.product.controller;

import com.adidos.product.dto.CategoryResponse;
import com.adidos.product.dto.ProductResponse;
import com.adidos.product.dto.ProductVariantResponse;
import com.adidos.product.entity.Category;
import com.adidos.product.service.CategoryService;
import com.adidos.product.service.ProductService;
import com.adidos.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    private final ReviewService reviewService;

    @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable Long id, Model model) {
        ProductResponse product = productService.getProductDetail(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", product.getVariants());

        Map<String, ProductVariantResponse> galleryVariants =
                product.getVariants()
                        .stream()
                        .collect(Collectors.toMap(
                                ProductVariantResponse::getColorName,
                                v -> v,
                                (existing, replacement) -> existing
                        ));

        model.addAttribute(
                "galleryVariants",
                new ArrayList<>(galleryVariants.values())
        );

        model.addAttribute("canReview", false);
        model.addAttribute("reviews", reviewService.getProductReviews(id));

        List<ProductResponse> relatedProducts = productService.getAllActiveProducts()
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .filter(p -> p.getCategoryId() != null
                        && product.getCategoryId() != null
                        && p.getCategoryId().equals(product.getCategoryId()))
                .limit(8)
                .toList();

        model.addAttribute("relatedProducts", relatedProducts);

        return "product/detail";
    }

    @GetMapping("/category/{id}")
    public String getProductsByCategory(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) List<String> materials,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String sort,
            Model model) {

        CategoryResponse currentCategory = categoryService.getById(id);

        Page<ProductResponse> productPage =
                productService.getProductsByCategoryIdPage(
                        id,
                        categoryIds,
                        minPrice,
                        maxPrice,
                        brands,
                        materials,
                        sort,
                        page,
                        size
                );

        Long parentId = currentCategory.getParentId() != null
                ? currentCategory.getParentId()
                : currentCategory.getId();

        model.addAttribute("category", currentCategory);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("selectedSort", sort);

        model.addAttribute("brands", productService.getBrandsByCategory(id));
        model.addAttribute("materials", productService.getMaterialsByCategory(id));

        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        model.addAttribute("selectedBrands", brands);
        model.addAttribute("selectedMaterials", materials);

        model.addAttribute("subCategories", categoryService.getSubCategories(parentId));
        model.addAttribute("selectedCategoryIds", categoryIds);

        return "product/category";
    }



}