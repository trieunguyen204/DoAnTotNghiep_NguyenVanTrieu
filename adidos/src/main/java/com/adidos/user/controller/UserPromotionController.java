package com.adidos.user.controller;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.service.ProductService;
import com.adidos.promotion.Promotion;
import com.adidos.promotion.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserPromotionController {

    private final PromotionRepository promotionRepository;
    private final ProductService productService;

    @GetMapping("/promotions/{id}")
    public String promotionDetail(
            @PathVariable("id") Long id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            Model model
    ) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        Page<ProductResponse> productPage =
                productService.getProductsForPromotionPage(promotion, page, size);

        model.addAttribute("promotion", promotion);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);

        return "promotion/detail";
    }
}