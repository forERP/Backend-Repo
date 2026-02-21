package com.forerp.erp.shipment.repository;

import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "inbound", "inbound.store", "inbound.warehouse",
            "outbound", "outbound.store", "outbound.items", "outbound.items.storeProduct", "outbound.items.storeProduct.warehouse"
    })
    Optional<Shipment> findById(Long id);

    Optional<Shipment> findByCarrierCodeAndTrackingNumber(String carrierCode, String trackingNumber);

    Optional<Shipment> findFirstByCarrierAndTrackingNumber(String carrier, String trackingNumber);

    @EntityGraph(attributePaths = {
            "inbound", "inbound.store", "inbound.warehouse",
            "outbound", "outbound.store", "outbound.items", "outbound.items.storeProduct", "outbound.items.storeProduct.warehouse"
    })
    @Query(value = """
            select distinct s
            from Shipment s
            left join s.inbound i
            left join s.outbound o
            left join o.items oi
            left join oi.storeProduct osp
            where (
                (:includeInbound = true and i is not null)
                or (:includeOutbound = true and o is not null)
            )
              and (:storeId is null or i.store.id = :storeId or o.store.id = :storeId)
              and (:warehouseId is null or i.warehouse.id = :warehouseId or osp.warehouse.id = :warehouseId)
              and (:status is null or s.status = :status)
              and (:fromDt is null or s.createdAt >= :fromDt)
              and (:toDt is null or s.createdAt < :toDt)
            order by s.createdAt desc
            """,
            countQuery = """
            select count(distinct s.id)
            from Shipment s
            left join s.inbound i
            left join s.outbound o
            left join o.items oi
            left join oi.storeProduct osp
            where (
                (:includeInbound = true and i is not null)
                or (:includeOutbound = true and o is not null)
            )
              and (:storeId is null or i.store.id = :storeId or o.store.id = :storeId)
              and (:warehouseId is null or i.warehouse.id = :warehouseId or osp.warehouse.id = :warehouseId)
              and (:status is null or s.status = :status)
              and (:fromDt is null or s.createdAt >= :fromDt)
              and (:toDt is null or s.createdAt < :toDt)
            """)
    Page<Shipment> search(
            @Param("includeInbound") boolean includeInbound,
            @Param("includeOutbound") boolean includeOutbound,
            @Param("storeId") Long storeId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") ShipmentStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );
}
