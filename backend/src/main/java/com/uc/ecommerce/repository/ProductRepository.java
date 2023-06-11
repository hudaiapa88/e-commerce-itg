package com.uc.ecommerce.repository;

import com.uc.ecommerce.model.dto.product.ProductResponse;
import com.uc.ecommerce.model.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query(value = "select p from #{#entityName} p " +
            "left join fetch p.category c " +
            "where (p.title like %:title%) " +
            "and (c.id in :categoryIds)"
            ,countQuery = "select count(p) from #{#entityName} p " +
            "where (p.title like %:title%) " +
            "and (p.category.id in :categoryIds)"
    )
    Page<ProductResponse> findByTitleContainingAndCategory_IdIn(@Param("title") String title,@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
}
