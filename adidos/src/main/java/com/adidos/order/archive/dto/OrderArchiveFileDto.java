package com.adidos.order.archive.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderArchiveFileDto {
    private String archiveVersion;
    private LocalDateTime archivedAt;
    private LocalDateTime archivedUntil;
    private Integer totalOrders;
    private List<ArchivedOrderDto> orders;
}