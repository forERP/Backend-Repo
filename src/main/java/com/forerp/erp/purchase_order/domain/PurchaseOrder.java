package com.forerp.erp.purchase_order.domain;

import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    @Column(length = 100)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrderItem> items = new ArrayList<>();

    /* ===== 생성 (요청 기반) ===== */
    public static PurchaseOrder createFromRequest(
            PurchaseRequest request,
            Supplier supplier,
            Store store,
            Warehouse warehouse,
            String memo,
            List<PurchaseOrderItem> items
    ) {
        if (request.getStatus() != PurchaseRequestStatus.APPROVED) {
            throw new IllegalStateException("승인되지 않은 발주 요청입니다.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.purchaseRequest = request;
        po.supplier = supplier;
        po.store = store;
        po.warehouse = warehouse;
        po.memo = memo;
        po.status = PurchaseOrderStatus.CREATED;
        po.createdAt = LocalDateTime.now();

        items.forEach(item -> {
            item.assignPurchaseOrder(po);
            po.items.add(item);
        });

        return po;
    }

    /* ===== 발주 확정 ===== */
    public void order() {
        if (this.status != PurchaseOrderStatus.CREATED) {
            throw new IllegalStateException("발주 가능한 상태가 아닙니다.");
        }
        this.status = PurchaseOrderStatus.ORDERED;
        this.orderedAt = LocalDateTime.now();
    }

    /* ===== 발주 취소 ===== */
    public void cancel() {
        if (this.status == PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("이미 발주된 주문은 취소할 수 없습니다.");
        }
        this.status = PurchaseOrderStatus.CANCELED;
    }

    /* ===== 발주 입고 완료 ===== */
    public void markReceived() {
        if (this.status != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("발주 상태가 ORDERED가 아닙니다.");
        }
        this.status = PurchaseOrderStatus.RECEIVED;
    }
}