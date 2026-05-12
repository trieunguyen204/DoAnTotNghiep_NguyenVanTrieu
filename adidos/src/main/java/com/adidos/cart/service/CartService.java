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

    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final PromotionService promotionService;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartByUser(String identifier, boolean isLogged) {
        if (isLogged) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return cartItemRepository.findByUserId(user.getId()).stream()
                    .map(this::toCartItemResponse)
                    .collect(Collectors.toList());
        } else {
            return cartItemRepository.findBySessionId(identifier).stream()
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
    public void removeCartItem(Long cartItemId, String identifier, boolean isLogged) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        validateCartItemOwner(item, identifier, isLogged);

        cartItemRepository.delete(item);
    }


    @Transactional
    public void addToCart(String identifier, boolean isLogged, CartItemRequest request) {
        ProductVariant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Số lượng tồn kho không đủ!");
        }

        User user = null;
        String sessionId = null;

        if (isLogged) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        } else {
            sessionId = identifier;
        }

        Optional<CartItem> existingItem = isLogged
                ? cartItemRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId())
                : cartItemRepository.findBySessionIdAndProductVariantId(sessionId, variant.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();

            int newQty = item.getQuantity() + request.getQuantity();

            if (newQty > variant.getStockQuantity()) {
                throw new RuntimeException("Vượt quá số lượng tồn kho!");
            }

            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .sessionId(sessionId)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .build();

            cartItemRepository.save(newItem);
        }
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(String identifier, boolean isLogged) {
        if (isLogged) {
            return userRepository.findByEmail(identifier)
                    .map(user -> cartItemRepository.findByUserId(user.getId()).stream()
                            .mapToInt(CartItem::getQuantity).sum())
                    .orElse(0);
        } else {

            return cartItemRepository.findBySessionId(identifier).stream()
                    .mapToInt(CartItem::getQuantity).sum();
        }
    }


    @Transactional
    public void updateQuantity(Long cartItemId,
                               Integer quantity,
                               String identifier,
                               boolean isLogged) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        validateCartItemOwner(item, identifier, isLogged);

        if (quantity == null) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        if (quantity > item.getProductVariant().getStockQuantity()) {
            throw new RuntimeException("Vượt quá số lượng tồn kho!");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void mergeGuestCartToUser(String sessionId, String userEmail) {
        if (sessionId == null || userEmail == null) {
            return;
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        List<CartItem> guestItems = cartItemRepository.findBySessionId(sessionId);

        if (guestItems.isEmpty()) {
            return;
        }

        for (CartItem guestItem : guestItems) {
            ProductVariant variant = guestItem.getProductVariant();

            Optional<CartItem> existingUserItem =
                    cartItemRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId());

            if (existingUserItem.isPresent()) {
                CartItem userItem = existingUserItem.get();

                int mergedQty = userItem.getQuantity() + guestItem.getQuantity();
                int maxStock = variant.getStockQuantity();

                userItem.setQuantity(Math.min(mergedQty, maxStock));

                cartItemRepository.save(userItem);
                cartItemRepository.delete(guestItem);
            } else {
                guestItem.setUser(user);
                guestItem.setSessionId(null);
                cartItemRepository.save(guestItem);
            }
        }
    }

    private void validateCartItemOwner(CartItem item, String identifier, boolean isLogged) {
        if (isLogged) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (item.getUser() == null || !item.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bạn không có quyền thao tác sản phẩm này");
            }
        } else {
            if (item.getSessionId() == null || !item.getSessionId().equals(identifier)) {
                throw new RuntimeException("Bạn không có quyền thao tác sản phẩm này");
            }
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