package com.adidos.cart.service;

import com.adidos.cart.dto.CartItemRequest;
import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.entity.CartItem;
import com.adidos.cart.mapper.CartMapper;
import com.adidos.cart.repository.CartItemRepository;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductVariantRepository;
import com.adidos.user.entity.User;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUserId(user.getId()).stream()
                .map(CartMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToCart(String email, CartItemRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductVariant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // Kiểm tra tồn kho
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Số lượng tồn kho không đủ!");
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        cartRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId())
                .ifPresentOrElse(
                        // Đã có -> Cộng dồn số lượng
                        existingItem -> {
                            int newQuantity = existingItem.getQuantity() + request.getQuantity();
                            if (newQuantity > variant.getStockQuantity()) {
                                throw new RuntimeException("Vượt quá số lượng tồn kho!");
                            }
                            existingItem.setQuantity(newQuantity);
                            cartRepository.save(existingItem);
                        },
                        // Chưa có -> Tạo mới
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .user(user)
                                    .productVariant(variant)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartRepository.save(newItem);
                        }
                );
    }

    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }
}