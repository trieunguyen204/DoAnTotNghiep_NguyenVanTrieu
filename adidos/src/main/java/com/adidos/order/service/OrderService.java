package com.adidos.order.service;

import com.adidos.cart.entity.CartItem;
import com.adidos.cart.repository.CartItemRepository;
import com.adidos.order.dto.CheckoutRequest;
import com.adidos.order.dto.OrderResponse;
import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.enums.PaymentStatus;
import com.adidos.order.mapper.OrderMapper;
import com.adidos.order.repository.OrderRepository;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductVariantRepository;
import com.adidos.user.entity.User;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    /**
     * LOGIC ĐẶT HÀNG (QUAN TRỌNG NHẤT)
     */
    @Transactional
    public Long placeOrder(String email, CheckoutRequest request) {
        // 1. Xác thực người dùng
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Lấy giỏ hàng của user
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, không thể đặt hàng");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Khởi tạo Đơn hàng (Order)
        Order order = Order.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .shippingAddress(request.getShippingAddress())
                .shippingFee(new BigDecimal("30000")) // Giả sử phí ship đồng giá 30k (Hoặc free nếu Adidos luxury)
                .discountAmount(BigDecimal.ZERO) // Sẽ xử lý voucher sau nếu có
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        // 4. Xử lý từng mặt hàng trong giỏ
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();

            // 4.1 Kiểm tra TỒN KHO lần cuối (Rất quan trọng, vì có thể lúc trong giỏ thì còn hàng, lúc bấm nút thì hết)
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm '" + variant.getProduct().getName() + "' - Size " +
                        variant.getSize().getSizeName() + " không đủ số lượng trong kho!");
            }

            // 4.2 Trừ Tồn Kho
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            variantRepository.save(variant);

            // 4.3 Tính tiền
            BigDecimal itemTotal = variant.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            // 4.4 Tạo Snapshot OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .price(variant.getPrice()) // Lấy giá ngay tại thời điểm mua
                    .quantity(cartItem.getQuantity())
                    .color(variant.getColor().getColorName())
                    .size(variant.getSize().getSizeName())
                    .build();

            orderItems.add(orderItem);
        }

        // 5. Cập nhật tổng tiền và lưu đơn hàng (Cascade = ALL sẽ tự lưu OrderItem)
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems); // Link 2 chiều
        Order savedOrder = orderRepository.save(order);

        // 6. Dọn dẹp Giỏ Hàng sau khi chốt đơn thành công
        cartItemRepository.deleteByUserId(user.getId());

        // 7. Trả về ID đơn hàng để redirect sang trang Thanh toán / Thành công
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }

        // Hoàn lại Tồn kho
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getProductVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }

        // Cập nhật trạng thái
        order.setOrderStatus(OrderStatus.CANCELLED);
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Ngăn chặn việc cập nhật lại đơn đã bị hủy hoặc đã hoàn thành (tùy logic nghiệp vụ)
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã hủy!");
        }

        order.setOrderStatus(newStatus);

        // Nếu cập nhật thành ĐÃ GIAO, có thể tự động chuyển PaymentStatus thành PAID (nếu là COD)
        if (newStatus == OrderStatus.DELIVERED && "COD".equals(order.getPaymentStatus().name())) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        orderRepository.save(order);
    }
}