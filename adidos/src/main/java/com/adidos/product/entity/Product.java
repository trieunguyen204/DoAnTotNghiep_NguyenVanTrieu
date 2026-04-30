package com.adidos.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String brand;
    private String material;
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String status;
    private java.time.LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }


    public String getPrimaryImageUrl() {
        if (variants == null || variants.isEmpty()) {
            return "/images/default.jpg";
        }

        for (ProductVariant variant : variants) {
            if (variant.getImages() != null && !variant.getImages().isEmpty()) {


                for (ProductImage image : variant.getImages()) {
                    if (Boolean.TRUE.equals(image.getIsPrimary())) {
                        return image.getImageUrl();
                    }
                }

                return variant.getImages().get(0).getImageUrl();
            }
        }

        return "/images/default.jpg";
    }

    public BigDecimal getDisplayPrice() {
        if (variants == null || variants.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return variants.get(0).getPrice();
    }

}