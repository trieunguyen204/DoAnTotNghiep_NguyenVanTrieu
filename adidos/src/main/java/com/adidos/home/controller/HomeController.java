package com.adidos.home.controller;

import com.adidos.product.service.ProductService;
import com.adidos.promotion.entity.Promotion;
import com.adidos.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final PromotionRepository promotionRepository;

    @GetMapping("/")
    public String homeUser(Model model){
        LocalDateTime now = LocalDateTime.now();

        // 1. KM Giảm sâu nhất
        Promotion topPromo = promotionRepository.findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDiscountValueDesc("ACTIVE", now, now);
        model.addAttribute("topPromo", topPromo);
        model.addAttribute("topPromoProducts", productService.getProductsForPromotion(topPromo));

        // 2. KM Sắp hết hạn
        Promotion soonPromo = promotionRepository.findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc("ACTIVE", now, now);
        model.addAttribute("soonPromo", soonPromo);
        model.addAttribute("soonPromoProducts", productService.getProductsForPromotion(soonPromo));

        // 3. Tất cả sản phẩm
        model.addAttribute("allProducts", productService.getAllActiveProducts());

        return "home/index";
    }
}