package com.adidos.admin;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProduct {
    private String productName;
    private Long quantitySold;
}