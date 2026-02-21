package com.forerp.erp.returns.repository;

import com.forerp.erp.returns.domain.ReturnStatus;
import com.forerp.erp.returns.domain.SalesReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    @EntityGraph(attributePaths = {"order", "payment", "store", "warehouse", "processedBy"})
    Optional<SalesReturn> findByOrder_Id(Long orderId);

    boolean existsByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"order", "payment", "store", "warehouse", "processedBy"})
    @Query("""
        select r from SalesReturn r
        where (:storeId is null or r.store.id = :storeId)
          and (:status is null or r.status = :status)
          and (:fromDt is null or r.processedAt >= :fromDt)
          and (:toDt is null or r.processedAt < :toDt)
        order by r.processedAt desc
        """)
    Page<SalesReturn> search(
            @Param("storeId") Long storeId,
            @Param("status") ReturnStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );
}

