package com.adidos.order.archive.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_archive_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderArchiveLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String filePath;

    private LocalDateTime archivedUntil;

    private Integer totalOrders;

    private String checksum;

    private LocalDateTime createdAt;

    private String createdBy;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}