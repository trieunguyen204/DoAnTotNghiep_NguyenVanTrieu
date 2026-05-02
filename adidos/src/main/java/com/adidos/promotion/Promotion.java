package com.adidos.promotion;

import com.adidos.product.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_name")
    private String promotionName;

    @Column(name = "discount_type")
    private String discountType; // "PERCENT" hoặc "FIXED"

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "max_discount_value")
    private BigDecimal maxDiscountValue;

    private Integer priority;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    private String status; // "ACTIVE", "INACTIVE"

    @ManyToMany
    @JoinTable(
            name = "promotion_category",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}