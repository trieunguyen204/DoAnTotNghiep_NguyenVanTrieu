package com.adidos.tryon.entity;

import com.adidos.product.entity.Product;
import com.adidos.product.entity.ProductVariant;
import com.adidos.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_try_on")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualTryOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "person_image_url", columnDefinition = "TEXT", nullable = false)
    private String personImageUrl;

    @Column(name = "garment_image_url", columnDefinition = "TEXT", nullable = false)
    private String garmentImageUrl;

    @Column(name = "result_image_url", columnDefinition = "TEXT")
    private String resultImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TryOnStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = TryOnStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
