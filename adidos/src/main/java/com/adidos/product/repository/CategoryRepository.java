package com.adidos.product.repository;

import com.adidos.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    List<Category> findByParentIsNull();


    List<Category> findByParentId(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:kw%")
    List<Category> searchCategories(@Param("kw") String keyword);


}