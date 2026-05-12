package com.adidos.admin.service;

import com.adidos.admin.dto.DashboardStats;
import com.adidos.admin.dto.ProductSalesStats;
import com.adidos.order.repository.OrderRepository;
import com.adidos.product.repository.ProductRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardStats getAllTimeStats() {
        return DashboardStats.builder()
                .totalRevenue(orderRepository.calculateTotalRevenueAllTime())
                .totalOrders(orderRepository.count())
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .build();
    }

    public List<Object[]> getRevenueChartByYear(int year) {
        return orderRepository.getMonthlyRevenueByYear(year);
    }

    public List<ProductSalesStats> getBestSellingProductsByYear(int year) {
        return orderRepository.getProductSalesStatsByYear(year)
                .stream()
                .map(this::mapProductSales)
                .filter(p -> p.getSoldQuantity() > p.getStockQuantity() * 0.1)
                .toList();
    }

    public List<ProductSalesStats> getSlowSellingProductsByYear(int year) {
        return orderRepository.getProductSalesStatsByYear(year)
                .stream()
                .map(this::mapProductSales)
                .filter(p -> p.getSoldQuantity() <= p.getStockQuantity() * 0.1)
                .toList();
    }

    private ProductSalesStats mapProductSales(Object[] row) {
        return ProductSalesStats.builder()
                .productId(((Number) row[0]).longValue())
                .productName((String) row[1])
                .soldQuantity(((Number) row[2]).longValue())
                .stockQuantity(((Number) row[3]).longValue())
                .build();
    }
}