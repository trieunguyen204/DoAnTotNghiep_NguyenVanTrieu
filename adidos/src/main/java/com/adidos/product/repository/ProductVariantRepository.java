package com.adidos.product.repository;

import com.adidos.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Lấy tất cả các biến thể của một sản phẩm
    List<ProductVariant> findByProductId(Long productId);

    // Kiểm tra xem một biến thể cụ thể (Product + Size + Color) đã tồn tại chưa
    Optional<ProductVariant> findByProductIdAndSizeIdAndColorId(Long productId, Long sizeId, Long colorId);


    List<ProductVariant> findByProductIdAndColorId(Long productId, Long colorId);

    @Query("""
    SELECT 
        v.product.id,
        v.product.name,
        COALESCE(SUM(v.stockQuantity), 0),
        COALESCE((
            SELECT SUM(oi.quantity)
            FROM OrderItem oi
            WHERE oi.productVariant.product.id = v.product.id
              AND oi.order.orderStatus <> com.adidos.order.enums.OrderStatus.CANCELLED
        ), 0)
    FROM ProductVariant v
    GROUP BY v.product.id, v.product.name
    """)
    List<Object[]> getProductSalesStats();


}