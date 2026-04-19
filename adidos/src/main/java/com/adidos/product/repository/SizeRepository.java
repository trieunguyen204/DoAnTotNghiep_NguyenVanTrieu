package com.adidos.product.repository;

import com.adidos.product.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {

    @Query("SELECT s FROM Size s WHERE s.sizeName LIKE %:kw%")
    List<Size> searchSizes(@Param("kw") String keyword);
    Optional<Size> findBySizeName(String sizeName);
}