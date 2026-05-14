package com.adidos.dashboard.service;

import com.adidos.dashboard.dto.DashboardStats;
import com.adidos.dashboard.dto.ProductSalesStats;
import com.adidos.order.repository.OrderRepository;
import com.adidos.product.repository.ProductRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public List<Object[]> getRevenueChart(LocalDateTime start,
                                          LocalDateTime end) {
        return orderRepository.getRevenueChart(start, end);
    }

    public List<ProductSalesStats> getBestSellingProducts(LocalDateTime start, LocalDateTime end) {
        return orderRepository.getSoldProductSalesStats(start, end)
                .stream()
                .map(this::mapProductSales)
                .sorted((a, b) -> Long.compare(b.getSoldQuantity(), a.getSoldQuantity()))
                .limit(5)
                .toList();
    }

    public List<ProductSalesStats> getSlowSellingProducts(LocalDateTime start, LocalDateTime end) {
        List<ProductSalesStats> all = productRepository.getAllProductSalesStats(start, end)
                .stream()
                .map(this::mapProductSales)
                .toList();

        List<Long> bestIds = getBestSellingProducts(start, end)
                .stream()
                .map(ProductSalesStats::getProductId)
                .toList();

        return all.stream()
                .filter(p -> !bestIds.contains(p.getProductId()))
                .sorted((a, b) -> Long.compare(a.getSoldQuantity(), b.getSoldQuantity()))
                .limit(5)
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