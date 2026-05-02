package com.adidos.cart.service;

import com.adidos.cart.dto.CartItemRequest;
import com.adidos.cart.dto.CartItemResponse;
import com.adidos.cart.entity.CartItem;
import com.adidos.cart.mapper.CartMapper;
import com.adidos.cart.repository.CartItemRepository;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.repository.ProductVariantRepository;
import com.adidos.promotion.PromotionService;
import com.adidos.user.entity.User;
import com.adidos.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartItemRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final PromotionService promotionService;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartByUser(String identifier, boolean isLogged) {
        if (isLogged) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return cartRepository.findByUserId(user.getId()).stream()
                    .map(this::toCartItemResponse)
                    .collect(Collectors.toList());
        } else {
            return cartRepository.findBySessionId(identifier).stream()
                    .map(this::toCartItemResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void clearCart(String identifier, boolean isLogged) {

        if (isLogged) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            cartItemRepository.deleteByUser(user);
        } else {
            cartItemRepository.deleteBySessionId(identifier);
        }
    }


    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }


    @Transactional
    public void addToCart(String identifier, boolean isLogged, CartItemRequest request) {
        ProductVariant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Số lượng tồn kho không đủ!");
        }

        User user = isLogged ? userRepository.findByEmail(identifier).orElse(null) : null;
        String sessionId = isLogged ? null : identifier;

        Optional<CartItem> existingItem = isLogged
                ? cartRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId())
                : cartRepository.findBySessionIdAndProductVariantId(sessionId, variant.getId());

        existingItem.ifPresentOrElse(
                item -> {
                    int newQty = item.getQuantity() + request.getQuantity();
                    if (newQty > variant.getStockQuantity()) throw new RuntimeException("Vượt quá số lượng tồn kho!");
                    item.setQuantity(newQty);
                    cartRepository.save(item);
                },
                () -> {
                    CartItem newItem = CartItem.builder()
                            .user(user)
                            .sessionId(sessionId)
                            .productVariant(variant)
                            .quantity(request.getQuantity())
                            .build();
                    cartRepository.save(newItem);
                }
        );
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(String identifier, boolean isLogged) {
        if (isLogged) {
            return userRepository.findByEmail(identifier)
                    .map(user -> cartRepository.findByUserId(user.getId()).stream()
                            .mapToInt(CartItem::getQuantity).sum())
                    .orElse(0);
        } else {

            return cartRepository.findBySessionId(identifier).stream()
                    .mapToInt(CartItem::getQuantity).sum();
        }
    }


    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (quantity > item.getProductVariant().getStockQuantity()) {
            throw new RuntimeException("Vượt quá số lượng tồn kho!");
        }
        if (quantity <= 0) {
            cartRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartRepository.save(item);
        }
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();

        Long categoryId = variant.getProduct().getCategory() != null
                ? variant.getProduct().getCategory().getId()
                : null;

        BigDecimal finalPrice = promotionService.calculateDiscountedPrice(
                categoryId,
                variant.getPrice()
        );

        return CartMapper.toResponse(cartItem, finalPrice);
    }
}