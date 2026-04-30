package com.adidos.admin.controller;


import com.adidos.admin.entity.DashboardStats;
import com.adidos.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public String dashboard(@RequestParam(value = "mode", defaultValue = "month") String mode,
                            @RequestParam(value = "year", required = false) Integer year,
                            Model model) {
        DashboardStats stats = dashboardService.getDashboardStats(mode, year);

        model.addAttribute("stats", stats);
        model.addAttribute("mode", mode);
        model.addAttribute("selectedYear", year != null ? year : java.time.LocalDate.now().getYear());

        return "admin/dashboard";
    }
}