package com.forerp.erp.storeproduct.repository;

import com.forerp.erp.storeproduct.domain.StoreProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    @EntityGraph(attributePaths = {"product"})
    Page<StoreProduct> findByStore_IdAndWarehouse_IdAndProduct_NameContaining(
            Long storeId, Long warehouseId, String keyword, Pageable pageable
    );

    @EntityGraph(attributePaths = {"product"})
    Page<StoreProduct> findByStore_IdAndProduct_NameContaining(
            Long storeId, String keyword, Pageable pageable
    );

    @EntityGraph(attributePaths = {"product"})
    Optional<StoreProduct> findByStore_IdAndWarehouse_IdAndProduct_Id(
            Long storeId, Long warehouseId, Long productId
    );

    @EntityGraph(attributePaths = {"product"})
    Optional<StoreProduct> findByStore_IdAndProduct_Id(
            Long storeId, Long productId
    );
}