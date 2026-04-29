package com.adidos.admin;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product-analytics")
public class ProductAnalyticsController {

    private final ProductAnalyticsService productAnalyticsService;

    @GetMapping
    public String index(@RequestParam(value = "type", defaultValue = "best-selling") String type,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        Model model) {
        List<ProductAnalyticsResponse> products = productAnalyticsService.getProductsByType(type);

        Pageable pageable = PageRequest.of(page, 5);
        int start = Math.min((int) pageable.getOffset(), products.size());
        int end = Math.min(start + pageable.getPageSize(), products.size());

        Page<ProductAnalyticsResponse> productPage =
                new PageImpl<>(products.subList(start, end), pageable, products.size());

        model.addAttribute("productPage", productPage);
        model.addAttribute("currentType", type);

        return "admin/product/product_analytics";
    }

    @GetMapping("/{productId}/variants")
    @ResponseBody
    public Object variantSales(@PathVariable Long productId) {
        return productAnalyticsService.getVariantSales(productId);
    }
}