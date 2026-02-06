package com.forerp.erp.outbound.repository;

import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    @EntityGraph(attributePaths = {"order", "store", "shipment", "items", "items.storeProduct", "items.storeProduct.warehouse"})
    @Query("""
        select distinct o
        from Outbound o
        join o.items oi
        join oi.storeProduct sp
        where (:storeId is null or o.store.id = :storeId)
          and (:status is null or o.status = :status)
          and (:warehouseId is null or sp.warehouse.id = :warehouseId)
          and (:fromDt is null or o.createdAt >= :fromDt)
          and (:toDt is null or o.createdAt < :toDt)
        order by o.createdAt desc
    """)
    Page<Outbound> search(
            @Param("storeId") Long storeId,
            @Param("status") OutboundStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    // 주문 취소 제약용
    boolean existsByOrder_Id(Long orderId);
}