package com.adidos.product.controller;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.service.CategoryService;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;


    @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable Long id, Model model) {
        ProductResponse product = productService.getProductDetail(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", product.getVariants());


        model.addAttribute("canReview", false);

        List<ProductResponse> allActive = productService.getAllActiveProducts();
        List<ProductResponse> relatedProducts = allActive.stream()
                .filter(p -> !p.getId().equals(id))
                .limit(12)
                .toList();

        model.addAttribute("relatedProducts", relatedProducts);



        return "product/detail";
    }

    @GetMapping("/category/{id}")
    public String getProductsByCategory(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String material,
            Model model) {

        model.addAttribute("category", categoryService.getById(id));

        model.addAttribute("products", productService.getProductsByCategoryId(id, minPrice, maxPrice, brand, material));

        model.addAttribute("brands", productService.getBrandsByCategory(id));
        model.addAttribute("materials", productService.getMaterialsByCategory(id));


        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedMaterial", material);

        return "product/category";
    }






}