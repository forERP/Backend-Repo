package com.forerp.erp.storeproduct.repository;

import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.store.dto.StoreProductListResponseDto;
import com.forerp.erp.storeproduct.domain.SaleStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    boolean existsByStore_IdAndWarehouse_IdAndProduct_Id(Long storeId, Long warehouseId, Long productId);

    @EntityGraph(attributePaths = {"store", "warehouse", "product"})
    @Query("""
        select sp from StoreProduct sp
        where sp.product.status = :status
          and (:storeId is null or sp.store.id = :storeId)
          and (:warehouseId is null or sp.warehouse.id = :warehouseId)
          and (
            :storeKeyword is null
            or lower(sp.store.name) like lower(concat('%', :storeKeyword, '%'))
            or lower(sp.store.storeCode) like lower(concat('%', :storeKeyword, '%'))
          )
          and (
            :warehouseKeyword is null
            or lower(sp.warehouse.name) like lower(concat('%', :warehouseKeyword, '%'))
            or lower(sp.warehouse.code) like lower(concat('%', :warehouseKeyword, '%'))
          )
          and (
            :productKeyword is null
            or lower(sp.product.name) like lower(concat('%', :productKeyword, '%'))
            or lower(sp.product.sku) like lower(concat('%', :productKeyword, '%'))
          )
          and (:saleStatus is null or sp.saleStatus = :saleStatus)
        """)
    Page<StoreProduct> searchInventory(
            @Param("storeId") Long storeId,
            @Param("warehouseId") Long warehouseId,
            @Param("storeKeyword") String storeKeyword,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("saleStatus") SaleStatus saleStatus,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"product"})
    Optional<StoreProduct> findByStore_IdAndWarehouse_IdAndProduct_Id(
            Long storeId, Long warehouseId, Long productId
    );

    @EntityGraph(attributePaths = {"product"})
    List<StoreProduct> findByStore_IdAndWarehouse_IdAndProduct_IdIn(
            Long storeId,
            Long warehouseId,
            List<Long> productIds
    );

    @EntityGraph(attributePaths = {"warehouse", "product"})
    List<StoreProduct> findByStore_IdAndWarehouse_IdInAndProduct_IdInAndProduct_Status(
            Long storeId,
            List<Long> warehouseIds,
            List<Long> productIds,
            ProductStatus status
    );

    @EntityGraph(attributePaths = {"store", "warehouse", "product"})
    Optional<StoreProduct> findByStore_IdAndWarehouse_IdAndProduct_IdAndProduct_Status(
            Long storeId, Long warehouseId, Long productId, ProductStatus status
    );

    @EntityGraph(attributePaths = {"store", "warehouse", "product"})
    Optional<StoreProduct> findFirstByStore_IdAndProduct_IdAndProduct_StatusOrderByUpdatedAtDesc(
            Long storeId, Long productId, ProductStatus status
    );

    @EntityGraph(attributePaths = {"store", "warehouse", "product"})
    @Query("select sp from StoreProduct sp where sp.id = :storeProductId")
    Optional<StoreProduct> findDetailById(@Param("storeProductId") Long storeProductId);

    @Query("""
        select sp.product.id from StoreProduct sp
        where sp.store.id = :storeId
          and sp.warehouse.id = :warehouseId
          and sp.product.id in :productIds
        """)
    Set<Long> findExistingProductIds(
            @Param("storeId") Long storeId,
            @Param("warehouseId") Long warehouseId,
            @Param("productIds") List<Long> productIds
    );

    @Query("""
        select new com.forerp.erp.store.dto.StoreProductListResponseDto(
            p.id,
            p.sku,
            p.name,
            p.category.name,
            p.category.imageUrl,
            p.imageUrl,
            p.msrpPrice,
            max(sp.id),
            coalesce(sum(sp.quantity), 0),
            case
                when sum(case when sp.saleStatus = com.forerp.erp.storeproduct.domain.SaleStatus.ON then 1 else 0 end) > 0
                    then com.forerp.erp.storeproduct.domain.SaleStatus.ON
                else com.forerp.erp.storeproduct.domain.SaleStatus.OFF
            end,
            coalesce(max(sp.salePrice), p.msrpPrice)
        )
        from Product p
        left join StoreProduct sp
            on p.id = sp.product.id
           and sp.store.id = :storeId
        where p.status = com.forerp.erp.product.domain.ProductStatus.ACTIVE
        group by p.id, p.sku, p.name, p.category.name, p.category.imageUrl, p.imageUrl, p.msrpPrice
        order by p.name asc
        """)
    Page<StoreProductListResponseDto> findAllProductsWithStoreInfo(
            @Param("storeId") Long storeId,
            Pageable pageable
    );

    @Query("""
        select count(distinct sp.product.id)
        from StoreProduct sp
        where (:storeId is null or sp.store.id = :storeId)
          and sp.product.status = com.forerp.erp.product.domain.ProductStatus.ACTIVE
          and sp.quantity <= sp.stockThreshold
        """)
    long countLowStockSkuForDashboard(@Param("storeId") Long storeId);
}
