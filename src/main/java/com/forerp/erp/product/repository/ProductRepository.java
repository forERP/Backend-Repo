package com.forerp.erp.product.repository;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    java.util.Optional<Product> findBySku(String sku);
    java.util.Optional<Product> findFirstByName(String name);

    java.util.List<Product> findAllByStatus(ProductStatus status);

    java.util.List<Product> findByCategory_IdOrderByNameAsc(Long categoryId);

    java.util.List<Product> findByStatusAndCategory_CodeNotOrderByNameAsc(ProductStatus status, String categoryCode);

    @Query("""
        select p from Product p
        where (
              :productKeyword is null
              or lower(p.name) like lower(concat('%', :productKeyword, '%'))
              or lower(p.sku) like lower(concat('%', :productKeyword, '%'))
          )
          and (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
          and (:sku is null or lower(p.sku) like lower(concat('%', :sku, '%')))
          and (:status is null or p.status = :status)
        """)
    Page<Product> search(
            @Param("productKeyword") String productKeyword,
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("status") ProductStatus status,
            Pageable pageable
    );
}
