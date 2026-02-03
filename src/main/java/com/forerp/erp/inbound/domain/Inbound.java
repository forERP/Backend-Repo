package com.forerp.erp.inbound.domain;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inbounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;   // 입고 받는 지점

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse; // 입고 창고

    @OneToOne(mappedBy = "inbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private Shipment shipment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "inbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InboundStatus status;

    /* ===== 생성 로직 ===== */
    public static Inbound create(
            PurchaseOrder purchaseOrder,
            Store store,
            Warehouse warehouse,
            List<InboundItem> items
    ) {
        Inbound inbound = new Inbound();
        inbound.purchaseOrder = purchaseOrder;
        inbound.store = store;
        inbound.warehouse = warehouse;
        inbound.createdAt = LocalDateTime.now();
        inbound.status = InboundStatus.CREATED;

        items.forEach(item -> {
            item.assignInbound(inbound);
            inbound.items.add(item);
        });

        return inbound;
    }

    /* ===== 연관관계 ===== */
    public void assignShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    /* ===== 상태 변경 ===== */
    public void changeStatus(InboundStatus status) {
        this.status = status;
    }

    /* ===== 입고 확정 ===== */
    public void confirm() {
        if (this.status == InboundStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 입고입니다.");
        }
        this.status = InboundStatus.CONFIRMED;
    }
}