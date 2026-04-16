package com.adidos.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "color")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "color_name", nullable = false, length = 100)
    private String colorName;
}