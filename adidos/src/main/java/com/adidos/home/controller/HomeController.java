package com.adidos.home.controller;

import com.adidos.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String homeUser(Model model){
        model.addAttribute("featuredProducts", productService.getAllActiveProducts());
        return "home/index";
    }
}