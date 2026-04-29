package com.adidos.admin;

import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;
import com.adidos.order.entity.Payment;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.enums.PaymentStatus;
import com.adidos.order.repository.OrderRepository;
import com.adidos.order.repository.PaymentRepository;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductRepository;
import com.adidos.product.repository.ProductVariantRepository;
import com.adidos.review.ReviewRepository;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;

    public DashboardStats getDashboardStats(String mode, Integer year) {
        List<Order> orders = orderRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();

        int selectedYear = year != null ? year : LocalDate.now().getYear();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .map(this::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayRevenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> o.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .map(this::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthRevenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> o.getCreatedAt().getYear() == LocalDate.now().getYear())
                .filter(o -> o.getCreatedAt().getMonthValue() == LocalDate.now().getMonthValue())
                .map(this::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yearRevenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> o.getCreatedAt().getYear() == selectedYear)
                .map(this::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ChartData chartData = buildRevenueChart(orders, mode, selectedYear);

        return DashboardStats.builder()
                .totalOrders((long) orders.size())
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalReviews(reviewRepository.count())
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .yearRevenue(yearRevenue)
                .pendingOrders(countStatus(orders, OrderStatus.PENDING))
                .processingOrders(countStatus(orders, OrderStatus.PROCESSING))
                .shippingOrders(countStatus(orders, OrderStatus.SHIPPING))
                .deliveredOrders(countStatus(orders, OrderStatus.DELIVERED))
                .cancelledOrders(countStatus(orders, OrderStatus.CANCELLED))
                .chartLabels(chartData.labels)
                .chartRevenue(chartData.revenue)
                .topSellingProducts(getTopSellingProducts(orders))
                .lowStockProducts(getLowStockProducts())
                .paymentMethodStats(getPaymentMethodStats(payments))
                .build();
    }

    private Long countStatus(List<Order> orders, OrderStatus status) {
        return orders.stream()
                .filter(o -> o.getOrderStatus() == status)
                .count();
    }

    private BigDecimal getFinalAmount(Order order) {
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal ship = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;

        return total.add(ship).subtract(discount);
    }

    private ChartData buildRevenueChart(List<Order> orders, String mode, int year) {
        if ("year".equalsIgnoreCase(mode)) {
            return buildYearChart(orders);
        }

        return buildMonthChart(orders, year);
    }

    private ChartData buildMonthChart(List<Order> orders, int year) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenue = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;

            labels.add("T" + currentMonth);

            BigDecimal monthRevenue = orders.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                    .filter(o -> o.getCreatedAt() != null)
                    .filter(o -> o.getCreatedAt().getYear() == year)
                    .filter(o -> o.getCreatedAt().getMonth() == Month.of(currentMonth))
                    .map(this::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            revenue.add(monthRevenue);
        }

        return new ChartData(labels, revenue);
    }

    private ChartData buildYearChart(List<Order> orders) {
        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - 4;

        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenue = new ArrayList<>();

        for (int year = startYear; year <= currentYear; year++) {
            final int y = year;

            labels.add(String.valueOf(y));

            BigDecimal yearRevenue = orders.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                    .filter(o -> o.getCreatedAt() != null)
                    .filter(o -> o.getCreatedAt().getYear() == y)
                    .map(this::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            revenue.add(yearRevenue);
        }

        return new ChartData(labels, revenue);
    }

    private List<TopSellingProduct> getTopSellingProducts(List<Order> orders) {
        Map<String, Long> productSales = new HashMap<>();

        orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .forEach(order -> {
                    if (order.getOrderItems() == null) return;

                    for (OrderItem item : order.getOrderItems()) {
                        String productName = item.getProductName();
                        Long qty = item.getQuantity() != null ? item.getQuantity().longValue() : 0L;

                        productSales.put(
                                productName,
                                productSales.getOrDefault(productName, 0L) + qty
                        );
                    }
                });

        return productSales.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> TopSellingProduct.builder()
                        .productName(e.getKey())
                        .quantitySold(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<LowStockProduct> getLowStockProducts() {
        List<ProductVariant> variants = productVariantRepository.findAll();

        return variants.stream()
                .filter(v -> v.getStockQuantity() != null && v.getStockQuantity() <= 10)
                .sorted(Comparator.comparing(ProductVariant::getStockQuantity))
                .limit(8)
                .map(v -> LowStockProduct.builder()
                        .productName(v.getProduct() != null ? v.getProduct().getName() : "Không xác định")
                        .variantInfo(
                                (v.getColor() != null ? v.getColor().getColorName() : "N/A")
                                        + " / "
                                        + (v.getSize() != null ? v.getSize().getSizeName() : "N/A")
                        )
                        .stockQuantity(v.getStockQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private List<PaymentMethodStat> getPaymentMethodStats(List<Payment> payments) {
        Map<String, Long> map = new HashMap<>();

        for (Payment payment : payments) {
            String method = payment.getPaymentMethod() != null
                    ? payment.getPaymentMethod()
                    : "UNKNOWN";

            map.put(method, map.getOrDefault(method, 0L) + 1);
        }

        return map.entrySet()
                .stream()
                .map(e -> PaymentMethodStat.builder()
                        .paymentMethod(e.getKey())
                        .total(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private record ChartData(List<String> labels, List<BigDecimal> revenue) {}
}