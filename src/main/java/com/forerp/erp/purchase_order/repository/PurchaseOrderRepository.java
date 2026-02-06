package com.forerp.erp.purchase_order.repository;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = {
            "supplier",
            "purchaseRequest",
            "store",
            "warehouse",
            "items",
            "items.product"
    })
    @Query("""
        select po from PurchaseOrder po
        where (:storeId is null or po.store.id = :storeId)
          and (:warehouseId is null or po.warehouse.id = :warehouseId)
          and (:status is null or po.status = :status)
          and (:fromDt is null or po.createdAt >= :fromDt)
          and (:toDt is null or po.createdAt < :toDt)
        """)
    Page<PurchaseOrder> search(
            @Param("storeId") Long storeId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseOrderStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "supplier","purchaseRequest","store","warehouse","items","items.product"
    })
    @Query("select po from PurchaseOrder po where po.id = :id")
    Optional<PurchaseOrder> findDetailById(@Param("id") Long id);

    boolean existsByPurchaseRequest_Id(Long purchaseRequestId);

    @EntityGraph(attributePaths = {"store", "warehouse", "items", "items.product"})
    @Query("select po from PurchaseOrder po where po.id = :id")
    Optional<PurchaseOrder> findForInboundCreate(@Param("id") Long id);
}