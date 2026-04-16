package com.adidos.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private String material;
    private String gender;
    private Long categoryId;
    private String status;
}