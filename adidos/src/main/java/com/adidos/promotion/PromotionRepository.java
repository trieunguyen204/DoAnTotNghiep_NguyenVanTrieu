package com.adidos.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Tìm các khuyến mãi đang chạy cho một danh mục cụ thể, sắp xếp theo ưu tiên cao nhất
    @Query("SELECT p FROM Promotion p JOIN p.categories c " +
            "WHERE c.id = :categoryId AND p.status = 'ACTIVE' " +
            "AND :now BETWEEN p.startDate AND p.endDate " +
            "ORDER BY p.priority DESC")
    List<Promotion> findActivePromotionsByCategory(@Param("categoryId") Long categoryId,
                                                   @Param("now") LocalDateTime now);

    // Lấy KM giảm sâu nhất
    Promotion findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDiscountValueDesc(String status, LocalDateTime now1, LocalDateTime now2);

    // Lấy KM sắp hết hạn
    Promotion findTopByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(String status, LocalDateTime now1, LocalDateTime now2);
}