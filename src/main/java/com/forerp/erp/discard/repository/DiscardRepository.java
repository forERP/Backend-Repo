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
            "warehouse"
    })
    @Query("""
        select d from Discard d
        where (:storeId is null or d.store.id = :storeId)
          and (:warehouseId is null or d.warehouse.id = :warehouseId)
          and (:status is null or d.status = :status)
          and (:fromDt is null or d.createdAt >= :fromDt)
          and (:toDt is null or d.createdAt < :toDt)
        """)
    Page<Discard> search(
            @Param("storeId") Long storeId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") DiscardStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );
}