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
import com.adidos.promotion.PromotionService;
import com.adidos.review.ReviewRepository;
import com.adidos.user.dto.AddressResponse;
import com.adidos.user.entity.User;
import com.adidos.user.repository.UserRepository;
import com.adidos.user.service.AddressService;
import com.adidos.voucher.Voucher;
import com.adidos.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final PromotionService promotionService;
    private final VoucherService voucherService;

    private static final BigDecimal SHIPPING_FEE = BigDecimal.ZERO;

    public Long placeOrder(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        AddressResponse address = resolveAddress(email, request.getAddressId());

        Order order = Order.builder()
                .user(user)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getPhone())
                .shippingAddress(address.getFullAddress())
                .orderStatus(OrderStatus.PENDING)
                .shippingFee(SHIPPING_FEE)
                .discountAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .build();

        buildItemsAndDecreaseStock(order, cartItems);

        // Guest không được dùng voucher
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(SHIPPING_FEE);

        order = orderRepository.save(order);
        createPayment(order, request.getPaymentMethod());

        cartItemRepository.deleteByUser(user);

        return order.getId();
    }

    public Long placeGuestOrder(String sessionId, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findBySessionId(sessionId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        String guestName = trim(request.getGuestName());
        String guestEmail = trim(request.getGuestEmail());
        String guestPhone = trim(request.getGuestPhone());
        String guestAddress = trim(request.buildGuestFullAddress());

        validateGuestInfo(guestName, guestEmail, guestPhone, guestAddress);

        Order order = Order.builder()
                .receiverName(guestName)
                .receiverPhone(guestPhone)
                .shippingAddress(guestAddress)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .guestAddress(guestAddress)
                .orderStatus(OrderStatus.PENDING)
                .shippingFee(SHIPPING_FEE)
                .discountAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .build();

        buildItemsAndDecreaseStock(order, cartItems);

        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(SHIPPING_FEE);

        order = orderRepository.save(order);
        createPayment(order, request.getPaymentMethod());

        cartItemRepository.deleteBySessionId(sessionId);

        return order.getId();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return attachReviewStatus(
                OrderMapper.toResponse(order, payment)
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getGuestOrderDetail(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() != null) {
            throw new RuntimeException("Đây không phải đơn hàng khách vãng lai");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return attachReviewStatus(
                OrderMapper.toResponse(order, payment)
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return orderRepository.findByUserIdWithItems(user.getId())
                .stream()
                .map(order -> OrderMapper.toResponse(
                        order,
                        paymentRepository.findByOrderId(order.getId()).orElse(null)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getGuestOrdersByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không hợp lệ");
        }

        return orderRepository.findGuestOrdersWithItemsByEmail(email)
                .stream()
                .map(order -> attachReviewStatus(
                        OrderMapper.toResponse(
                                order,
                                paymentRepository.findByOrderId(order.getId()).orElse(null)
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String status) {
        List<Order> orders;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            orders = orderRepository.findAll();
        } else {
            OrderStatus orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            orders = orderRepository.findByOrderStatus(orderStatus);
        }

        return orders.stream()
                .map(order -> attachReviewStatus(
                        OrderMapper.toResponse(
                                order,
                                paymentRepository.findByOrderId(order.getId()).orElse(null)
                        )
                ))
                .toList();
    }

    public void markManualTransferWaitingConfirm(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xác nhận đơn này");
        }

        if (OrderStatus.CANCELLED.equals(order.getOrderStatus())) {
            throw new RuntimeException("Đơn hàng đã bị hủy");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new RuntimeException("Đơn hàng đã thanh toán");
        }

        payment.setPaymentMethod("BANK_TRANSFER");
        payment.setStatus(PaymentStatus.UNPAID);
        paymentRepository.save(payment);
    }

    public void markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (OrderStatus.CANCELLED.equals(order.getOrderStatus())) {
            throw new RuntimeException("Không thể xác nhận thanh toán cho đơn đã hủy");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        if (OrderStatus.PENDING.equals(order.getOrderStatus())) {
            order.setOrderStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
    }

    public void markPayOSPaid(Long orderId) {
        markPaid(orderId);
    }

    public void markPaymentFailedAndCancel(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new RuntimeException("Đơn đã thanh toán, không thể hủy bằng trạng thái thanh toán thất bại");
        }

        if (!OrderStatus.CANCELLED.equals(order.getOrderStatus())) {
            restoreStock(order);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }

        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == newStatus) {
            return;
        }

        validateStatusTransition(currentStatus, newStatus);

        if (OrderStatus.CANCELLED.equals(newStatus)) {
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
                throw new RuntimeException("Đơn đã thanh toán, không được hủy trực tiếp");
            }

            restoreStock(order);

            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }

        order.setOrderStatus(newStatus);

        if (OrderStatus.DELIVERED.equals(newStatus)) {
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

            if (payment != null && "COD".equalsIgnoreCase(payment.getPaymentMethod())) {
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            }
        }

        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!OrderStatus.PENDING.equals(order.getOrderStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new RuntimeException("Đơn đã thanh toán, không thể hủy");
        }

        restoreStock(order);

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void moveToNextStatus(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        OrderStatus current = order.getOrderStatus();

        if (OrderStatus.CANCELLED.equals(current)) {
            throw new RuntimeException("Đơn đã hủy, không thể chuyển trạng thái");
        }

        if (OrderStatus.DELIVERED.equals(current)) {
            throw new RuntimeException("Đơn đã hoàn thành");
        }

        OrderStatus nextStatus;

        if (OrderStatus.PENDING.equals(current)) {
            nextStatus = OrderStatus.PROCESSING;
        } else if (OrderStatus.PROCESSING.equals(current)) {
            nextStatus = OrderStatus.SHIPPING;
        } else if (OrderStatus.SHIPPING.equals(current)) {
            nextStatus = OrderStatus.DELIVERED;
        } else {
            throw new RuntimeException("Trạng thái đơn không hợp lệ");
        }

        updateOrderStatus(orderId, nextStatus);
    }

    private void buildItemsAndDecreaseStock(Order order, List<CartItem> cartItems) {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = variantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())) {
                throw new RuntimeException("Sản phẩm đã ngừng bán");
            }

            Integer qty = cartItem.getQuantity();
            if (qty == null || qty <= 0) {
                throw new RuntimeException("Số lượng sản phẩm không hợp lệ");
            }

            if (variant.getStockQuantity() == null || variant.getStockQuantity() < qty) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName() + " không đủ tồn kho");
            }

            Long categoryId = variant.getProduct().getCategory() != null
                    ? variant.getProduct().getCategory().getId()
                    : null;

            BigDecimal finalPrice = promotionService.calculateDiscountedPrice(categoryId, variant.getPrice());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .price(finalPrice)
                    .quantity(qty)
                    .color(variant.getColor() != null ? variant.getColor().getColorName() : null)
                    .size(variant.getSize() != null ? variant.getSize().getSizeName() : null)
                    .build();

            order.addOrderItem(orderItem);

            variant.setStockQuantity(variant.getStockQuantity() - qty);
            variantRepository.save(variant);

            total = total.add(finalPrice.multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalPrice(total);
    }

    private void applyVoucher(Order order, String voucherCode) {
        BigDecimal total = order.getTotalPrice() != null
                ? order.getTotalPrice()
                : BigDecimal.ZERO;

        if (voucherCode == null || voucherCode.isBlank()) {
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setVoucher(null);
            order.setShippingFee(SHIPPING_FEE);
            return;
        }

        Voucher voucher = voucherService.getValidVoucher(voucherCode, total);

        if (order.getUser() != null
                && orderRepository.existsByUserIdAndVoucherId(
                order.getUser().getId(),
                voucher.getId()
        )) {
            throw new RuntimeException("Bạn đã sử dụng voucher này rồi");
        }

        BigDecimal discount = voucherService.calculateDiscount(voucherCode, total);

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(total) > 0) {
            discount = total;
        }

        order.setVoucher(voucher);
        order.setDiscountAmount(discount);
        order.setShippingFee(SHIPPING_FEE);
    }

    private void createPayment(Order order, String method) {
        String paymentMethod = normalizePaymentMethod(method);

        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal ship = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;

        BigDecimal finalAmount = total.add(ship).subtract(discount);

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .amount(finalAmount)
                .status(PaymentStatus.UNPAID)
                .build();

        paymentRepository.save(payment);
        order.setPayment(payment);
    }

    private void restoreStock(Order order) {
        if (order.getOrderItems() == null) return;

        for (OrderItem item : order.getOrderItems()) {
            if (item.getProductVariant() == null || item.getQuantity() == null) {
                continue;
            }

            ProductVariant variant = variantRepository.findById(item.getProductVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể khi hoàn kho"));

            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }
    }

    private AddressResponse resolveAddress(String email, Long addressId) {
        List<AddressResponse> addresses = addressService.getMyAddresses(email);

        if (addresses.isEmpty()) {
            throw new RuntimeException("Bạn chưa có địa chỉ giao hàng");
        }

        if (addressId != null) {
            return addresses.stream()
                    .filter(address -> addressId.equals(address.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ"));
        }

        return addresses.stream()
                .filter(address -> Boolean.TRUE.equals(address.getIsDefault()))
                .findFirst()
                .orElse(addresses.get(0));
    }

    private void validateGuestInfo(String name, String email, String phone, String address) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Tên người nhận không được để trống");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (address == null || address.isBlank()) {
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (OrderStatus.CANCELLED.equals(current)) {
            throw new RuntimeException("Đơn đã hủy, không thể cập nhật");
        }

        if (OrderStatus.DELIVERED.equals(current)) {
            throw new RuntimeException("Đơn đã giao, không thể cập nhật");
        }

        if (OrderStatus.PENDING.equals(current)
                && !(OrderStatus.PROCESSING.equals(next) || OrderStatus.CANCELLED.equals(next))) {
            throw new RuntimeException("PENDING chỉ được chuyển sang PROCESSING hoặc CANCELLED");
        }

        if (OrderStatus.PROCESSING.equals(current)
                && !(OrderStatus.SHIPPING.equals(next) || OrderStatus.CANCELLED.equals(next))) {
            throw new RuntimeException("PROCESSING chỉ được chuyển sang SHIPPING hoặc CANCELLED");
        }

        if (OrderStatus.SHIPPING.equals(current)
                && !OrderStatus.DELIVERED.equals(next)) {
            throw new RuntimeException("SHIPPING chỉ được chuyển sang DELIVERED");
        }
    }

    private String normalizePaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return "COD";
        }

        String value = method.trim().toUpperCase();

        return switch (value) {
            case "COD", "PAYOS", "BANK_TRANSFER", "QR" -> value;
            default -> throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        };
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }


    @Transactional(readOnly = true)
    public OrderResponse getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return attachReviewStatus(
                OrderMapper.toResponse(order, payment)
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrdersPage(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return orderRepository.findByUserIdOrderByIdDesc(user.getId(), pageable)
                .map(order -> OrderMapper.toResponse(
                        order,
                        paymentRepository.findByOrderId(order.getId()).orElse(null)
                ));
    }


    @Transactional
    public void cancelOrder(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (!OrderStatus.PENDING.equals(order.getOrderStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn đang chờ xác nhận");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new RuntimeException("Đơn hàng đã thanh toán, không thể hủy");
        }

        restoreStock(order);

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    private OrderResponse attachReviewStatus(OrderResponse response) {
        if (response == null || response.getItems() == null) {
            return response;
        }

        response.getItems().forEach(item ->
                item.setReviewed(
                        reviewRepository.existsByOrderItemId(item.getId())
                )
        );

        return response;
    }
}