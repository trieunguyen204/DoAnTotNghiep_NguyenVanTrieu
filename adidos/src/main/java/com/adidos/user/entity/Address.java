package com.adidos.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}