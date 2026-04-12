package com.adidos.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_provider")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProvider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String provider; // LOCAL, GOOGLE, FACEBOOK
    private String providerId;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }
}