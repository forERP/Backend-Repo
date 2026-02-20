package com.forerp.erp.discard.repository;

import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.domain.DiscardStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DiscardRepository extends JpaRepository<Discard, Long> {

    @EntityGraph(attributePaths = {
            "store",
            "warehouse",
            "createdBy",
            "items",
            "items.storeProduct",
            "items.storeProduct.product"
    })
    @Query("select d from Discard d where d.id = :id")
    Optional<Discard> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "store",
            "warehouse",
            "createdBy"
    })
    @Query("""
        select distinct d from Discard d
        left join d.items di
        left join di.storeProduct sp
        left join sp.product p
        where (:storeId is null or d.store.id = :storeId)
          and (
              :storeKeyword is null
              or lower(d.store.name) like lower(concat('%', :storeKeyword, '%'))
              or lower(d.store.storeCode) like lower(concat('%', :storeKeyword, '%'))
          )
          and (
              :warehouseKeyword is null
              or lower(d.warehouse.name) like lower(concat('%', :warehouseKeyword, '%'))
              or lower(d.warehouse.code) like lower(concat('%', :warehouseKeyword, '%'))
          )
          and (
              :productKeyword is null
              or lower(p.name) like lower(concat('%', :productKeyword, '%'))
              or lower(p.sku) like lower(concat('%', :productKeyword, '%'))
          )
          and (:warehouseId is null or d.warehouse.id = :warehouseId)
          and (:status is null or d.status = :status)
          and (:fromDt is null or d.createdAt >= :fromDt)
          and (:toDt is null or d.createdAt < :toDt)
          and (:discardedFromDt is null or d.discardedAt >= :discardedFromDt)
          and (:discardedToDt is null or d.discardedAt < :discardedToDt)
        order by d.createdAt desc
        """)
    Page<Discard> search(
            @Param("storeId") Long storeId,
            @Param("storeKeyword") String storeKeyword,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("warehouseId") Long warehouseId,
            @Param("status") DiscardStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("discardedFromDt") LocalDateTime discardedFromDt,
            @Param("discardedToDt") LocalDateTime discardedToDt,
            Pageable pageable
    );
}
