package com.adidos.user.controller;

import com.adidos.product.dto.ProductResponse;
import com.adidos.product.service.ProductService;
import com.adidos.promotion.entity.Promotion;
import com.adidos.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final PromotionRepository promotionRepository;

    @GetMapping("/")
    public String homeUser(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {

        LocalDateTime now = LocalDateTime.now();

        // =========================
        // 1. Promotion giảm mạnh nhất
        // =========================
        Promotion topPromo = promotionRepository
                .findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDiscountValueDesc(
                        "ACTIVE",
                        now,
                        now
                );

        model.addAttribute("topPromo", topPromo);

        model.addAttribute(
                "topPromoProducts",
                topPromo != null
                        ? productService.getProductsForPromotion(topPromo)
                        : List.of()
        );

        // =========================
        // 2. Promotion sắp hết hạn
        // =========================
        Promotion soonPromo = promotionRepository
                .findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(
                        "ACTIVE",
                        now,
                        now
                );

        model.addAttribute("soonPromo", soonPromo);

        model.addAttribute(
                "soonPromoProducts",
                soonPromo != null
                        ? productService.getProductsForPromotion(soonPromo)
                        : List.of()
        );

        // =========================
        // 3. Pagination toàn bộ sản phẩm
        // =========================
        Page<ProductResponse> productPage =
                productService.getActiveProductsPage(page, size);

        model.addAttribute("productPage", productPage);
        model.addAttribute("allProducts", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);

        return "home/index";
    }
}