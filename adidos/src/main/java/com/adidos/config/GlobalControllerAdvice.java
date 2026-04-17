package com.adidos.config;

import com.adidos.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CategoryService categoryService;


    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("rootCategories", categoryService.getRootCategories());
    }
}