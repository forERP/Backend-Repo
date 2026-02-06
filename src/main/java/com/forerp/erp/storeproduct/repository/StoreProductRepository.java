package com.forerp.erp.storeproduct.repository;

import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.store.dto.StoreProductListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT new com.forerp.erp.store.dto.StoreProductListResponseDto(" +
            "p.id, p.sku, p.name, p.category.name, p.msrpPrice, " +
            "sp.id, sp.quantity, sp.saleStatus, sp.salePrice) " +
            " FROM Product p " +
            " LEFT JOIN StoreProduct sp ON p.id = sp.product.id AND sp.store.id = :storeId " +
            " WHERE p.status = 'ACTIVE'")
    Page<StoreProductListResponseDto> findAllProductsWithStoreInfo(
            @Param("storeId") Long storeId,
            Pageable pageable
    );
}