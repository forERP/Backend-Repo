package com.forerp.erp.inbound.repository;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

    @EntityGraph(attributePaths = {"store", "warehouse", "shipment"})
    @Query("""
        select i
        from Inbound i
        where (:storeId is null or i.store.id = :storeId)
          and (:storeName is null or lower(i.store.name) like lower(concat('%', :storeName, '%')))
          and (:storeCode is null or lower(i.store.storeCode) like lower(concat('%', :storeCode, '%')))
          and (:status is null or i.status = :status)
          and (:fromDt is null or i.createdAt >= :fromDt)
          and (:toDt is null or i.createdAt < :toDt)
        order by i.createdAt desc
    """)
    Page<Inbound> search(
            @Param("storeId") Long storeId,
            @Param("storeName") String storeName,
            @Param("storeCode") String storeCode,
            @Param("status") InboundStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );
}
