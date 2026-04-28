package com.adidos.review;

import com.adidos.order.repository.OrderItemRepository;
import com.adidos.product.repository.ProductRepository;
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
                .stream().map(ReviewMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void addReview(String email, ReviewRequest request) {
        Review review = new Review();
        review.setUser(userRepository.findByEmail(email).orElseThrow());
        review.setProduct(productRepository.findById(request.getProductId()).orElseThrow());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (request.getOrderItemId() != null) {
            orderItemRepository.findById(request.getOrderItemId()).ifPresent(item -> {
                review.setOrderItem(item);
                review.setProductVariant(item.getProductVariant());
            });
        }
        reviewRepository.save(review);
    }
}