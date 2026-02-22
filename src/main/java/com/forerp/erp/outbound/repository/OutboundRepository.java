package com.forerp.erp.outbound.repository;

import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    @EntityGraph(attributePaths = {"order", "store", "shipment", "items", "items.storeProduct", "items.storeProduct.warehouse"})
    @Query("""
        select distinct o
        from Outbound o
        join o.items oi
        join oi.storeProduct sp
        where (:storeId is null or o.store.id = :storeId)
          and (
              :storeKeyword is null
              or lower(o.store.name) like lower(concat('%', :storeKeyword, '%'))
              or lower(o.store.storeCode) like lower(concat('%', :storeKeyword, '%'))
          )
          and (:storeName is null or lower(o.store.name) like lower(concat('%', :storeName, '%')))
          and (:storeCode is null or lower(o.store.storeCode) like lower(concat('%', :storeCode, '%')))
          and (:status is null or o.status = :status)
          and (:shipmentStatus is null or o.shipment.status = :shipmentStatus)
          and (:warehouseId is null or sp.warehouse.id = :warehouseId)
          and (:fromDt is null or o.createdAt >= :fromDt)
          and (:toDt is null or o.createdAt < :toDt)
        order by o.createdAt desc
    """)
    Page<Outbound> search(
            @Param("storeId") Long storeId,
            @Param("storeKeyword") String storeKeyword,
            @Param("storeName") String storeName,
            @Param("storeCode") String storeCode,
            @Param("status") OutboundStatus status,
            @Param("shipmentStatus") ShipmentStatus shipmentStatus,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    // 주문 취소 제약용
    boolean existsByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"order", "store", "shipment", "items", "items.orderItem", "items.storeProduct", "items.storeProduct.warehouse"})
    Optional<Outbound> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"order", "store", "shipment", "items", "items.orderItem", "items.storeProduct", "items.storeProduct.warehouse"})
    List<Outbound> findByOrder_IdIn(List<Long> orderIds);

    @Query("""
        select count(o) from Outbound o
        where (:storeId is null or o.store.id = :storeId)
          and o.status in :statuses
          and o.createdAt >= :fromDt
          and o.createdAt < :toDt
        """)
    long countForDashboard(
            @Param("storeId") Long storeId,
            @Param("statuses") List<OutboundStatus> statuses,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt
    );

    @Query("""
        select count(o) from Outbound o
        where (:storeId is null or o.store.id = :storeId)
          and o.status = :status
        """)
    long countByStatusForDashboard(
            @Param("storeId") Long storeId,
            @Param("status") OutboundStatus status
    );
}
