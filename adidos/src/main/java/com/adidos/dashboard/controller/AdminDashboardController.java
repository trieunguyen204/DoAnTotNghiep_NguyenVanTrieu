package com.adidos.dashboard.controller;

import com.adidos.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Year;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/admin/dashboard", "/admin"})
    public String dashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        LocalDate start = (startDate != null && !startDate.isBlank())
                ? LocalDate.parse(startDate)
                : LocalDate.now().withDayOfMonth(1);

        LocalDate end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        model.addAttribute("stats", dashboardService.getAllTimeStats());

        model.addAttribute(
                "revenueChart",
                dashboardService.getRevenueChart(start.atStartOfDay(), end.atTime(23, 59, 59))
        );

        model.addAttribute(
                "bestProducts",
                dashboardService.getBestSellingProducts(start.atStartOfDay(), end.atTime(23, 59, 59))
        );

        model.addAttribute(
                "slowProducts",
                dashboardService.getSlowSellingProducts(start.atStartOfDay(), end.atTime(23, 59, 59))
        );

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/best-products")
    public String bestProducts(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Model model) {

        LocalDate start = (startDate != null && !startDate.isBlank())
                ? LocalDate.parse(startDate)
                : LocalDate.now().withDayOfMonth(1);

        LocalDate end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        model.addAttribute(
                "products",
                dashboardService.getBestSellingProducts(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                )
        );

        model.addAttribute("title", "Danh sách sản phẩm bán chạy từ " + start + " đến " + end);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "admin/dashboard/product_sales_list";
    }

    @GetMapping("/admin/dashboard/slow-products")
    public String slowProducts(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Model model) {

        LocalDate start = (startDate != null && !startDate.isBlank())
                ? LocalDate.parse(startDate)
                : LocalDate.now().withDayOfMonth(1);

        LocalDate end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        model.addAttribute(
                "products",
                dashboardService.getSlowSellingProducts(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                )
        );

        model.addAttribute("title", "Danh sách sản phẩm bán ế từ " + start + " đến " + end);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "admin/dashboard/product_sales_list";
    }
}