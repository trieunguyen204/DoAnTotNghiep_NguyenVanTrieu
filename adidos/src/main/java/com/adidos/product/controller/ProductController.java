package com.adidos.product.controller;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;


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
}