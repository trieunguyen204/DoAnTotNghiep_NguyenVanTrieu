package com.adidos.review;

import com.adidos.order.entity.OrderItem;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.repository.OrderItemRepository;
import com.adidos.product.repository.ProductRepository;
import com.adidos.user.entity.User;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndParentIsNullOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addReview(String email, ReviewRequest request) {
        if (request.getOrderItemId() == null) {
            throw new RuntimeException("Thiếu thông tin sản phẩm trong đơn hàng");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong đơn hàng"));

        if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá sản phẩm này");
        }

        if (orderItem.getOrder().getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Chỉ được đánh giá sau khi đơn hàng đã giao thành công");
        }

        if (reviewRepository.existsByOrderItemId(orderItem.getId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(orderItem.getProductVariant().getProduct());
        review.setProductVariant(orderItem.getProductVariant());
        review.setOrderItem(orderItem);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllRootReviews() {
        return reviewRepository.findByParentIsNullOrderByCreatedAtDesc()
                .stream()
                .map(ReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminReplyReview(Long reviewId, String adminEmail, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Nội dung phản hồi không được để trống");
        }

        Review parent = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        Review reply = Review.builder()
                .user(admin)
                .product(parent.getProduct())
                .productVariant(parent.getProductVariant())
                .parent(parent)
                .rating(null)
                .comment(content.trim())
                .build();

        reviewRepository.save(reply);
    }
}