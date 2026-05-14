package com.adidos.order.archive.repository;

import com.adidos.order.archive.entity.OrderArchiveLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderArchiveLogRepository extends JpaRepository<OrderArchiveLog, Long> {

    List<OrderArchiveLog> findAllByOrderByCreatedAtDesc();
}