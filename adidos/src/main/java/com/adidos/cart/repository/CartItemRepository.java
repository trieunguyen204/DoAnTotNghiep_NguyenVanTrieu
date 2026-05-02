package com.adidos.cart.repository;

import com.adidos.cart.entity.CartItem;
import com.adidos.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {


    @Query("SELECT c FROM CartItem c " +
            "JOIN FETCH c.productVariant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH v.size " +
            "JOIN FETCH v.color " +
            "WHERE c.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);


    Optional<CartItem> findByUserIdAndProductVariantId(Long userId, Long variantId);


    void deleteByUserId(Long userId);

    List<CartItem> findBySessionId(String sessionId);
    Optional<CartItem> findBySessionIdAndProductVariantId(String sessionId, Long productVariantId);

    void deleteByUser(User user);

    void deleteBySessionId(String sessionId);
}