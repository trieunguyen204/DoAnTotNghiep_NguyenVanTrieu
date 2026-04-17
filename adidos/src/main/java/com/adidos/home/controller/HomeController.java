package com.adidos.home.controller;

import com.adidos.product.service.CategoryService;
import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String homeUser(Model model){
        // Lấy danh mục gốc và sản phẩm đang bán
        model.addAttribute("rootCategories", categoryService.getRootCategories());
        model.addAttribute("featuredProducts", productService.getAllActiveProducts());
        return "home/index";
    }
}