package com.adidos.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sizes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_name", nullable = false, length = 50)
    private String sizeName;
}