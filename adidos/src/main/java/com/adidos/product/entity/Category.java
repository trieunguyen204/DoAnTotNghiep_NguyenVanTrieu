package com.adidos.product.entity;

import com.adidos.promotion.entity.Promotion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private List<Category> subCategories;

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Product> products;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Promotion> promotions;

}