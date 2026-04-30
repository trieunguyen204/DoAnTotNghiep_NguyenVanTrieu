package com.adidos.admin.entity;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private Long totalReviews;

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal yearRevenue;

    private Long pendingOrders;
    private Long processingOrders;
    private Long shippingOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    private List<String> chartLabels;
    private List<BigDecimal> chartRevenue;

    private List<TopSellingProduct> topSellingProducts;
    private List<LowStockProduct> lowStockProducts;
    private List<PaymentMethodStat> paymentMethodStats;
}