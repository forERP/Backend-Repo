package com.forerp.erp.purchase_req.repository;

import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    @EntityGraph(attributePaths = {
            "store",
            "requestedBy"
    })
    @Query("""
        select pr from PurchaseRequest pr
        where (:storeId is null or pr.store.id = :storeId)
          and (
              :storeKeyword is null
              or lower(pr.store.name) like lower(concat('%', :storeKeyword, '%'))
              or lower(pr.store.storeCode) like lower(concat('%', :storeKeyword, '%'))
          )
          and (:storeName is null or lower(pr.store.name) like lower(concat('%', :storeName, '%')))
          and (:storeCode is null or lower(pr.store.storeCode) like lower(concat('%', :storeCode, '%')))
          and (:status is null or pr.status = :status)
          and (:fromDt is null or pr.createdAt >= :fromDt)
          and (:toDt is null or pr.createdAt < :toDt)
        """)
    Page<PurchaseRequest> search(
            @Param("storeId") Long storeId,
            @Param("storeKeyword") String storeKeyword,
            @Param("storeName") String storeName,
            @Param("storeCode") String storeCode,
            @Param("status") PurchaseRequestStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "store", "requestedBy", "items", "items.product"
    })
    @Query("select pr from PurchaseRequest pr where pr.id = :id")
    PurchaseRequest findDetailById(@Param("id") Long id);

    @Query("""
        select count(pr) from PurchaseRequest pr
        where (:storeId is null or pr.store.id = :storeId)
          and pr.status = :status
        """)
    long countByStatusForDashboard(
            @Param("storeId") Long storeId,
            @Param("status") PurchaseRequestStatus status
    );
}
