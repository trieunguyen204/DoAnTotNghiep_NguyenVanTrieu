package com.adidos.product.repository;

import com.adidos.product.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    @Query("SELECT c FROM Color c WHERE c.colorName LIKE %:kw%")
    List<Color> searchColors(@Param("kw") String keyword);

    Optional<Color> findByColorName(String colorName);
}