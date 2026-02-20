package com.forerp.erp.product.repository;

import com.forerp.erp.product.domain.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select c
            from ProductCategory c
            where (:name is null or lower(c.name) like lower(concat('%', :name, '%')))
              and (:code is null or lower(c.code) like lower(concat('%', :code, '%')))
            order by c.createdAt desc
            """)
    Page<ProductCategory> search(
            @Param("name") String name,
            @Param("code") String code,
            Pageable pageable
    );
}
