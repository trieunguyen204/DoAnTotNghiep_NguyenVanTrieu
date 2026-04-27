package com.adidos.order.service;

import com.adidos.cart.entity.CartItem;
import com.adidos.cart.repository.CartItemRepository;
import com.adidos.order.dto.CheckoutRequest;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;
import com.adidos.order.entity.Payment;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.enums.PaymentStatus;
import com.adidos.order.mapper.OrderMapper;
import com.adidos.order.repository.OrderRepository;
import com.adidos.order.repository.PaymentRepository;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductVariantRepository;
import com.adidos.promotion.service.PromotionService;
import com.adidos.user.entity.Address;
import com.adidos.user.entity.User;
import com.adidos.user.repository.AddressRepository;
import com.adidos.user.repository.UserRepository;
import com.adidos.voucher.entity.Voucher;
import com.adidos.voucher.repository.VoucherRepository;
import com.adidos.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionService promotionService;
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;


    /**
     * LOGIC ĐẶT HÀNG (QUAN TRỌNG NHẤT)
     */
    @Transactional
    public Long placeOrder(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Vui lòng chọn địa chỉ nhận hàng"));

        if (!address.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Địa chỉ không hợp lệ");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, không thể đặt hàng");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal shippingFee = new BigDecimal("30000");
        BigDecimal discountAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        String fullAddress = String.format("%s, %s, %s, %s",
                address.getAddressDetail(),
                address.getWard(),
                address.getDistrict(),
                address.getProvince());

        Order order = Order.builder()
                .user(user)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getPhone())
                .shippingAddress(fullAddress)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        for (CartItem cartItem : cartItems) {

            ProductVariant variant = cartItem.getProductVariant();

            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm '" + variant.getProduct().getName() + "' không đủ tồn kho");
            }

            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            variantRepository.save(variant);

            Long categoryId = variant.getProduct().getCategory() != null
                    ? variant.getProduct().getCategory().getId()
                    : null;

            BigDecimal finalPrice = promotionService.calculateDiscountedPrice(
                    categoryId,
                    variant.getPrice()
            );

            BigDecimal itemTotal = finalPrice.multiply(new BigDecimal(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .price(finalPrice)
                    .quantity(cartItem.getQuantity())
                    .color(variant.getColor().getColorName())
                    .size(variant.getSize().getSizeName())
                    .build();

            orderItems.add(orderItem);
        }

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            discountAmount = voucherService.calculateDiscount(request.getVoucherCode(), totalPrice);

            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

            order.setVoucherId(voucher.getId());

        }

        order.setDiscountAmount(discountAmount);
        order.setTotalPrice(totalPrice);

        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        BigDecimal finalAmount = totalPrice.add(shippingFee).subtract(discountAmount);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.getPaymentMethod())
                .amount(finalAmount)
                .status("PENDING")
                .build();

        if ("COD".equals(request.getPaymentMethod())) {
            payment.setStatus("PENDING");
            savedOrder.setPaymentStatus(PaymentStatus.UNPAID);
        }

        if ("QR_MANUAL".equals(request.getPaymentMethod())) {
            payment.setStatus("WAITING_TRANSFER");
            savedOrder.setPaymentStatus(PaymentStatus.UNPAID);
        }

        if ("PAYOS".equals(request.getPaymentMethod())) {
            payment.setStatus("PENDING");
            savedOrder.setPaymentStatus(PaymentStatus.UNPAID);
        }

        paymentRepository.save(payment);
        orderRepository.save(savedOrder);

        cartItemRepository.deleteByUserId(user.getId());

        return savedOrder.getId();
    }

    /**
     * LẤY DANH SÁCH LỊCH SỬ ĐƠN HÀNG CỦA USER
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Sử dụng hàm đã viết Custom @Query để lấy luôn Items, tránh N+1 Query
        List<Order> orders = orderRepository.findByUserIdWithItems(user.getId());

        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * LẤY CHI TIẾT 1 ĐƠN HÀNG CỤ THỂ
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, String email) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Bảo mật: Kiểm tra xem đơn hàng này có đúng là của User đang đăng nhập không
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return OrderMapper.toResponse(order);
    }

    /**
     * HỦY ĐƠN HÀNG (Chỉ áp dụng khi đơn đang PENDING)
     */
    @Transactional
    public void cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }

        restoreStock(order);

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
        });

        orderRepository.save(order);
    }

    // --- CÁC HÀM DÀNH RIÊNG CHO ADMIN ---

    /**
     * Lấy toàn bộ đơn hàng trong hệ thống (Mới nhất xếp lên đầu)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xem chi tiết đơn hàng (Không cần check email của User)
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailForAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return OrderMapper.toResponse(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã hủy!");
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                if ("COD".equalsIgnoreCase(payment.getPaymentMethod())) {
                    payment.setStatus("SUCCESS");
                    paymentRepository.save(payment);

                    order.setPaymentStatus(PaymentStatus.PAID);

                    increaseVoucherUsedCount(order);
                }
            });
        }

        orderRepository.save(order);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getProductVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }
    }

    private void increaseVoucherUsedCount(Order order) {
        if (order.getVoucherId() == null) return;

        Voucher voucher = voucherRepository.findById(order.getVoucherId())
                .orElse(null);

        if (voucher == null) return;

        voucher.setUsedCount(voucher.getUsedCount() == null ? 1 : voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }

    @Transactional
    public void markPayOSPaid(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setPaymentStatus(PaymentStatus.PAID);

        increaseVoucherUsedCount(order);

        orderRepository.save(order);
    }

    @Transactional
    public void markPaymentFailedAndCancel(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        restoreStock(order);

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);

        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForAdminByStatus(String status) {
        List<Order> orders;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {

            String cleanStatus = status.split(",")[0].trim().toUpperCase();

            OrderStatus orderStatus = OrderStatus.valueOf(cleanStatus);
            orders = orderRepository.findByOrderStatus(orderStatus);
        }

        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt đơn hàng đang chờ xác nhận");
        }

        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    @Transactional
    public int approveSelectedOrders(List<Long> orderIds) {
        int count = 0;

        for (Long id : orderIds) {
            Order order = orderRepository.findById(id).orElse(null);

            if (order != null && order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                count++;
            }
        }

        return count;
    }

    @Transactional
    public int approveAllOrdersByStatus(String currentStatus) {
        List<Order> orders;

        if (currentStatus == null || currentStatus.isBlank() || "ALL".equalsIgnoreCase(currentStatus)) {
            orders = orderRepository.findByOrderStatus(OrderStatus.PENDING);
        } else {
            OrderStatus status = OrderStatus.valueOf(currentStatus.toUpperCase());
            orders = orderRepository.findByOrderStatus(status);
        }

        int count = 0;

        for (Order order : orders) {
            if (order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                count++;
            }
        }

        return count;
    }


}