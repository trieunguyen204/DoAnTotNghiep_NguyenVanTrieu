package com.adidos.admin.controller;

import com.adidos.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/admin/dashboard", "/admin"})
    public String dashboard(@RequestParam(required = false) Integer year,
                            Model model) {

        int selectedYear = year != null ? year : Year.now().getValue();

        model.addAttribute("stats", dashboardService.getAllTimeStats());
        model.addAttribute("revenueChart", dashboardService.getRevenueChartByYear(selectedYear));
        model.addAttribute("bestProducts", dashboardService.getBestSellingProductsByYear(selectedYear));
        model.addAttribute("slowProducts", dashboardService.getSlowSellingProductsByYear(selectedYear));
        model.addAttribute("selectedYear", selectedYear);

        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/best-products")
    public String bestProducts(@RequestParam(required = false) Integer year,
                               Model model) {
        int selectedYear = year != null ? year : Year.now().getValue();

        model.addAttribute("products", dashboardService.getBestSellingProductsByYear(selectedYear));
        model.addAttribute("title", "Danh sách sản phẩm bán chạy năm " + selectedYear);
        model.addAttribute("selectedYear", selectedYear);

        return "admin/dashboard/product_sales_list";
    }

    @GetMapping("/admin/dashboard/slow-products")
    public String slowProducts(@RequestParam(required = false) Integer year,
                               Model model) {
        int selectedYear = year != null ? year : Year.now().getValue();

        model.addAttribute("products", dashboardService.getSlowSellingProductsByYear(selectedYear));
        model.addAttribute("title", "Danh sách sản phẩm bán ế năm " + selectedYear);
        model.addAttribute("selectedYear", selectedYear);

        return "admin/dashboard/product_sales_list";
    }
}