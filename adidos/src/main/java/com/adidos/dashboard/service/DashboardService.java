package com.adidos.dashboard.service;

import com.adidos.order.enums.OrderStatus;
import com.adidos.order.repository.OrderRepository;
import com.adidos.product.repository.ProductRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Tổng doanh thu
        stats.put("totalRevenue", orderRepository.calculateTotalRevenue());

        // 2. Tổng số đơn hàng
        stats.put("totalOrders", orderRepository.count());

        // 3. Đơn hàng chờ xử lý (PENDING)
        stats.put("pendingOrders", orderRepository.countByOrderStatus(OrderStatus.PENDING));

        // 4. Tổng số khách hàng
        stats.put("totalUsers", userRepository.countByRole("USER"));

        // 5. Tổng số sản phẩm
        stats.put("totalProducts", productRepository.count());

        return stats;
    }
}