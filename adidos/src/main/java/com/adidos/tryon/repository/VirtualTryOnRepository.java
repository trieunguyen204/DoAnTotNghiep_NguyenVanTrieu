package com.adidos.tryon.repository;

import com.adidos.tryon.entity.VirtualTryOn;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualTryOnRepository extends JpaRepository<VirtualTryOn, Long> {

    List<VirtualTryOn> findTop20ByProductIdAndSessionIdOrderByCreatedAtDesc(
            Long productId,
            String sessionId
    );
}